package org.wso2.carbon.gateway.core.flow;


import org.wso2.carbon.gateway.core.config.ConfigRegistry;
import org.wso2.carbon.gateway.core.config.IntegrationConfigHolder;
import org.wso2.carbon.gateway.core.config.Parameter;
import org.wso2.carbon.gateway.core.config.ParameterHolder;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.invokers.Fork;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.invokers.Join;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.manipulators.SleepMediator;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.manipulators.log.LogMediator;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * Test class for testing RxJava implementation
 */
public class RxTest {

    private static IntegrationConfigHolder ich = new IntegrationConfigHolder("RxTest");

    private Resource resource;
    private Worker cars;
    private Worker hotels;

    private LogMediator defaultWorkerStartLog;
    private LogMediator defaultWorkerDoneLog;
    private LogMediator carsLog;
    private LogMediator hotelsLog;

    private Fork parallelMediator;
    private Join receiveMediator;

    private SleepMediator carsSleep;
    private SleepMediator hotelsSleep;

    public RxTest() {
        resource = new Resource("resource");
        ich.addWorker(resource.getDefaultWorker());
    }

    public void run() {
        // Default Worker Start Log
        defaultWorkerStartLog = new LogMediator();
        Parameter level = new Parameter("level", "CUSTOM");
        Parameter category = new Parameter("category", "INFO");
        Parameter msg = new Parameter("Message", "In Resource's default worker");
        ParameterHolder defaultWorkerStartLogParams = new ParameterHolder();
        defaultWorkerStartLogParams.addParameter(level);
        defaultWorkerStartLogParams.addParameter(category);
        defaultWorkerStartLogParams.addParameter(msg);
        defaultWorkerStartLog.setParameters(defaultWorkerStartLogParams);

        // Default Worker Log
        defaultWorkerDoneLog = new LogMediator();
        Parameter dlevel = new Parameter("level", "CUSTOM");
        Parameter dCategory = new Parameter("category", "INFO");
        Parameter dMsg = new Parameter("Message", "Default worker done!");
        ParameterHolder defaultWorkerDoneLogParams = new ParameterHolder();
        defaultWorkerDoneLogParams.addParameter(dlevel);
        defaultWorkerDoneLogParams.addParameter(dCategory);
        defaultWorkerDoneLogParams.addParameter(dMsg);
        defaultWorkerDoneLog.setParameters(defaultWorkerDoneLogParams);

        // Cars Log
        carsLog = new LogMediator();
        Parameter cLevel = new Parameter("level", "CUSTOM");
        Parameter cCategory = new Parameter("category", "INFO");
        Parameter cMsg = new Parameter("Message", "In car worker");
        ParameterHolder carLogParams = new ParameterHolder();
        carLogParams.addParameter(cLevel);
        carLogParams.addParameter(cCategory);
        carLogParams.addParameter(cMsg);
        carsLog.setParameters(carLogParams);

        // Cars Log done
        LogMediator carsLogDone = new LogMediator();
        Parameter cdLevel = new Parameter("level", "CUSTOM");
        Parameter cdCategory = new Parameter("category", "INFO");
        Parameter cdMsg = new Parameter("Message", "Car worker done");
        ParameterHolder carLogDoneParams = new ParameterHolder();
        carLogDoneParams.addParameter(cdLevel);
        carLogDoneParams.addParameter(cdCategory);
        carLogDoneParams.addParameter(cdMsg);
        carsLogDone.setParameters(carLogDoneParams);

        // Hotels Log
        hotelsLog = new LogMediator();
        Parameter hLevel = new Parameter("level", "CUSTOM");
        Parameter hCategory = new Parameter("category", "INFO");
        Parameter hMsg = new Parameter("Message", "In hotel worker");
        ParameterHolder hotelLogParams = new ParameterHolder();
        hotelLogParams.addParameter(hLevel);
        hotelLogParams.addParameter(hCategory);
        hotelLogParams.addParameter(hMsg);
        hotelsLog.setParameters(hotelLogParams);

        // Hotels Log done
        LogMediator hotelsLogDone = new LogMediator();
        Parameter hdLevel = new Parameter("level", "CUSTOM");
        Parameter hdCategory = new Parameter("category", "INFO");
        Parameter hdMsg = new Parameter("Message", "Hotel worker done");
        ParameterHolder hotelsLogDoneParams = new ParameterHolder();
        hotelsLogDoneParams.addParameter(hdLevel);
        hotelsLogDoneParams.addParameter(hdCategory);
        hotelsLogDoneParams.addParameter(hdMsg);
        hotelsLogDone.setParameters(hotelsLogDoneParams);

        // Car Sleep
        carsSleep = new SleepMediator(6000);
        // Hotel Sleep
        hotelsSleep = new SleepMediator(3000);

        // Cars worker
        MediatorCollection carMediators = new MediatorCollection();
        carMediators.addMediator(carsLog);
        carMediators.addMediator(carsSleep);
        carMediators.addMediator(carsLogDone);
        cars = new Worker("cars", carMediators);

        // Hotels worker
        MediatorCollection hotelMediators = new MediatorCollection();
        hotelMediators.addMediator(hotelsLog);
        hotelMediators.addMediator(hotelsSleep);
        hotelMediators.addMediator(hotelsLogDone);
        hotels = new Worker("hotels", hotelMediators);

        // Add car + hotel worker to IntegrationConfigHolder
        ich.addWorker(cars);
        ich.addWorker(hotels);

        // Parallel Mediator
        String[] workerList = {"cars", "hotels"};
        List<String> workers = Arrays.asList(workerList);
        parallelMediator = new Fork("RxTest", workers);

        // Receive Mediator
        receiveMediator = new Join("RxTest", workers, true);

        resource.getDefaultWorker().addMediator(defaultWorkerStartLog);
        resource.getDefaultWorker().addMediator(parallelMediator);
        resource.getDefaultWorker().addMediator(receiveMediator);
        resource.getDefaultWorker().addMediator(defaultWorkerDoneLog);

        CarbonMessage carbonMessage = prepareCarbonMessage(new DefaultCarbonMessage(false));

        resource.receive(carbonMessage, null);
    }


    public static void main(String[] args) {
        org.apache.log4j.BasicConfigurator.configure();

        ConfigRegistry.getInstance().addGWConfig(ich);

        RxTest test = new RxTest();
        test.run();
    }

    private CarbonMessage prepareCarbonMessage(CarbonMessage cMsg) {
        cMsg.setHeader("Content-Type", "text/plain");
        cMsg.addMessageBody(ByteBuffer.wrap("Test data".getBytes(Charset.defaultCharset())));
        cMsg.setAlreadyRead(true);
        return cMsg;
    }
}
