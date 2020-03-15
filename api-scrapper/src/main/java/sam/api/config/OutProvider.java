package sam.api.config;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

import sam.myutils.Checker;

public interface OutProvider {
	public static PrintStream create(final String config) throws IOException {
		String s = Checker.isEmptyTrimmed(config) ? null : config.trim();
		
		if(s == null || s.equalsIgnoreCase("system_out")) {
			return System.out;
		}
		if(s.toLowerCase().startsWith("file:") || s.toLowerCase().startsWith("file_system_out:")) {
			String file = config.substring(s.indexOf(':') + 1).trim();
			if(file.isEmpty())
				throw new IllegalStateException("no filename specified in: \""+config+"\"");
			
			OutputStream os = Files.newOutputStream(Paths.get(file), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			os.write(("---------------------------------" + LocalDateTime.now() + "---------------------------------").getBytes());
			
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}));
			
			if(s.toLowerCase().startsWith("file_system_out:")) {
				return new PrintStream(new OutputStream() {
					@Override
					public void write(int b) throws IOException {
						System.out.write(b);
						os.write(b);
					}
					@Override
					public void write(byte[] b) throws IOException {
						System.out.write(b);
						os.write(b);
					}
					@Override
					public void write(byte[] b, int off, int len) throws IOException {
						System.out.write(b, off, len);
						os.write(b, off, len);
					}
					@Override
					public void flush() throws IOException {
						System.out.flush();
						os.flush();
					}
					@Override
					public void close() throws IOException {
						os.close();
					}
				});
			} else {
				return new PrintStream(os, true);
			}
		}
		
		throw new IOException("bad specification of out: \""+config+"\"");
	}


}
