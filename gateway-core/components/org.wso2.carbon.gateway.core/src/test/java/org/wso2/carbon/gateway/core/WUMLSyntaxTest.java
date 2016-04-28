package org.wso2.carbon.gateway.core;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.TokenStream;

import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.runtime.tree.ParseTree;

import org.junit.Assert;
import org.junit.Test;

import org.wso2.carbon.gateway.core.config.dsl.external.wuml.generated.WUMLLexer;
import org.wso2.carbon.gateway.core.config.dsl.external.wuml.generated.WUMLParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit test case for WUML Syntax check against ANTLR Syntax Tree
 */
public class WUMLSyntaxTest {

    @Test
    public void testSyntaxErrorsRouterIflow() {
        makeSyntaxCheckAssertion(getSyntaxTreeErrors("/integration-flows/router.iflow"));
    }

    @Test
    public void testSyntaxErrorsConstantsIflow() {
        makeSyntaxCheckAssertion(getSyntaxTreeErrors("/integration-flows/constants.iflow"));
    }

    @Test
    public void testSyntaxErrorsVariableAssignmentIflow() {
        makeSyntaxCheckAssertion(getSyntaxTreeErrors("/integration-flows/variable-assignment.iflow"));
    }

    private void makeSyntaxCheckAssertion(SyntaxErrorListener errorListener) {
        Assert.assertTrue("Below are syntax errors found: \n\n" + errorListener.toString(),
                errorListener.getSyntaxErrors().size() == 0);
    }

    private SyntaxErrorListener getSyntaxTreeErrors(String iFlowResource) {
        InputStream inputStream = null;

        try {
            File file = new File(getClass().getResource(iFlowResource).getFile());
            inputStream = new FileInputStream(file);

            CharStream cs = new ANTLRInputStream(inputStream);
            TokenStream tokenStream = new CommonTokenStream(new WUMLLexer(cs));
            WUMLParser parser = new WUMLParser(tokenStream);
            SyntaxErrorListener errorListener = new SyntaxErrorListener();
            parser.addErrorListener(errorListener);
            ParseTree tree = parser.script();
            return errorListener;

        } catch (Exception e) {
            return null;
        }
    }

    class SyntaxErrorListener extends BaseErrorListener {
        private final List<String> syntaxErrors = new ArrayList<String>();

        public List<String> getSyntaxErrors() {
            return syntaxErrors;
        }

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                                String msg, RecognitionException e) {
            super.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e);
            syntaxErrors.add(line + ":" + charPositionInLine + " " + msg);
        }

        @Override
        public String toString() {
            return Utils.join(syntaxErrors.iterator(), "\n");
        }

        public boolean hasErrors() {
            if (syntaxErrors.size() > 0) {
                return true;
            }

            return false;
        }
    }
}
