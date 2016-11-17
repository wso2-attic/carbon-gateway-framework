/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.config;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.ballerina.core.Constants;
import org.wso2.ballerina.core.flow.Mediator;
import org.wso2.ballerina.core.flow.Subroutine;
import org.wso2.ballerina.core.flow.mediators.builtin.flowcontrollers.filter.FilterMediator;
import org.wso2.ballerina.core.flow.mediators.builtin.manipulators.PropertyMediator;
import org.wso2.ballerina.core.flow.mediators.builtin.manipulators.log.LogMediator;
import org.wso2.carbon.deployment.engine.Artifact;

import java.io.File;
import java.util.List;

/**
 * This class test Configuration parsing and object model building for Subroutines
 */
public class WUMLSubroutineTest {

    @Test
    public void testSubroutineObjectCreation() {
        String iFlowResource = "/integration-flows/subroutine.ballerina";
        String integrationName = "subroutine";
        String subroutineName = "sampleSubroutine";

        Artifact testArtifact = new Artifact(new File(getClass().getResource(iFlowResource).getFile()));
        Assert.assertTrue("Error while deploying config :" + iFlowResource, TestUtil.deployArtifacts(testArtifact));
        // Get the Integration
        Integration sampleIntegration = IntegrationConfigRegistry.getInstance().getIntegrationConfig(integrationName);
        Assert.assertTrue("Integration is not deployed", sampleIntegration != null);
        // Get the local subroutine
        Subroutine deployedSubroutine = sampleIntegration.getSubroutine(subroutineName);
        Assert.assertTrue("Subroutine is not deployed", deployedSubroutine != null);
        // Get the collection of mediators inside the Subroutine
        List<Mediator> mediatorList = deployedSubroutine.getSubroutineMediators().getMediators();

        /* 1. Evaluate the generated mediator collection of the Subroutine */

        Assert.assertTrue("Number of generated mediator objects is incorrect.", mediatorList.size() == 3);

        Assert.assertTrue("Mediator object is not a Property mediator instance as expected. ",
                (mediatorList.get(0) instanceof LogMediator));
        Assert.assertTrue("Mediator object is not a Log mediator instance as expected. ",
                (mediatorList.get(1) instanceof PropertyMediator));
        Assert.assertTrue("Mediator object is not a Property mediator instance as expected. ",
                (mediatorList.get(2) instanceof FilterMediator));

        // expand the filter mediator
        List<Mediator> thenMediatorList = ((FilterMediator) mediatorList.get(2)).getChildThenMediatorList()
                .getMediators();
        List<Mediator> elseMediatorList = ((FilterMediator) mediatorList.get(2)).getChildOtherwiseMediatorList()
                .getMediators();

        // expand the if-then block
        Assert.assertTrue("Mediator object is not a Property mediator instance as expected. ",
                (thenMediatorList.get(0) instanceof PropertyMediator));
        Assert.assertTrue("Mediator object is not a Log mediator instance as expected. ",
                (thenMediatorList.get(1) instanceof LogMediator));

        // expand the else block
        Assert.assertTrue("Mediator object is not a Log mediator instance as expected. ",
                (elseMediatorList.get(0) instanceof LogMediator));

        /* 2. Evaluate Input Arguments of the Subroutine */
        String argumentOne = "m";
        String argumentTwo = "x";
        Assert.assertTrue("Argument object is not created in deployed subroutine",
                          deployedSubroutine.getInputArgs().get(argumentOne) == Constants.TYPES.MESSAGE);
        Assert.assertTrue("Argument object is not created in deployed subroutine",
                deployedSubroutine.getInputArgs().get(argumentTwo) == Constants.TYPES.STRING);

        /* 3. Evaluate Return Types of the Subroutine */
        Assert.assertTrue("Return types are not correctly added to the Subroutine object",
                deployedSubroutine.getReturnTypes().size() == 2);
        Assert.assertTrue("Return type is not correctly added to the Subroutine object",
                deployedSubroutine.getReturnTypes().get(0) == Constants.TYPES.MESSAGE);
        Assert.assertTrue("Return type is not correctly added to the Subroutine object",
                deployedSubroutine.getReturnTypes().get(1) == Constants.TYPES.INTEGER);

        /* 4. Evaluate Return Value Identifiers of the Subroutine */
        String returnValueIDOne = "m";
        String returnValueIDTwo = "a";
        Assert.assertTrue("Return value identifiers are not correctly added to the Subroutine object",
                deployedSubroutine.getReturnVariables().size() == 2);
        Assert.assertTrue("Return value identifier is correctly added to the Subroutine object",
                returnValueIDOne.equals(deployedSubroutine.getReturnVariables().get(0)));
        Assert.assertTrue("Return value identifier is correctly added to the Subroutine object",
                returnValueIDTwo.equals(deployedSubroutine.getReturnVariables().get(1)));

        // Remove created Integration
        IntegrationConfigRegistry.getInstance().removeIntegrationConfig(sampleIntegration);
    }

    //TODO: Test cases should be added on parsing "SubroutineCallMediator", after finalizing the Language syntax
}
