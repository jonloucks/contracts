package io.github.jonloucks.contracts.test.run;

import io.github.jonloucks.contracts.test.Tools;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@ExtendWith(RunTests.MyExtension.class)
public class RunTests implements Tests,
    Tests {

    public static class MyExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

        public MyExtension() {

        }

        @Override
        public void afterTestExecution(ExtensionContext extensionContext) {
            Tools.clean();
        }
        
        @Override
        public void beforeTestExecution(ExtensionContext extensionContext) {
            Tools.clean();
        }
    }
}
