package utils;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import javafx.application.Platform;

public class LoggerUtils extends AppenderBase<ILoggingEvent> {
    private final StringBuilder
            log = new StringBuilder();
    
    @Override
    protected void append(ILoggingEvent e) {
        if (log.length() > (128 * 1024)) {
            log.delete(0, log.length());
        }
        log.append("☆ ").append(e.getTimeStamp()).append(" ").append(e.getLevel().levelStr).append(" [").append(e.getLoggerName()).append("]\r\n\t\t").append(e.getFormattedMessage()).append("\r\n");
        Platform.runLater(() -> { 

        });
    }
}
