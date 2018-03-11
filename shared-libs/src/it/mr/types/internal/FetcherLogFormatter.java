package it.mr.types.internal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Created by mirco on 18/04/16.
 */
public class FetcherLogFormatter extends Formatter {

	private static final DateFormat df = new SimpleDateFormat("hh:mm:ss");

	@Override
	public String format(LogRecord record) {
		StringBuilder builder = new StringBuilder();
		builder.append(df.format(new Date(record.getMillis()))+" - ");
		builder.append(record.getLoggerName());
		builder.append(String.format(" %s  ",("["+record.getLevel()+"]")));
		builder.append(formatMessage(record)+"\n");
		return builder.toString();
	}
}
