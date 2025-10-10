package io.github.jonloucks.contracts.runtests;

import io.github.jonloucks.contracts.test.Tools;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(RunTests.MyExtension.class)
public class RunTests implements
    io.github.jonloucks.contracts.test.Tests,
    InternalTests {

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
