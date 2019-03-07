package org.demo.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class StartupListener {
	
	private static final Logger LOG = LoggerFactory.getLogger(StartupListener.class);
	
	private static String findKey = "data.file";
	
	private static String dataFile = "/data.json";
	
	private static JSONObject dataJson = new JSONObject();
	
	public static String getDataFile() {
		return dataFile;
	}
	
	public void setDataFile(String dataFile) {
		StartupListener.dataFile = dataFile;
	}
	
	public static JSONObject getData() {
		return dataJson;
	}
	
    @EventListener(ApplicationReadyEvent.class)
	public void startup() {
    	 String configFile = System.getProperty(findKey);
         LOG.debug("config File = {}", configFile);
         this.setDataFile(configFile);
         
         //初始化文件数据
         refreshData();
         
         //监控文件变化
         monitorFileChange();
         
         LOG.info("已启动文件监听.........");
	}
    
    private void refreshData() {
    	InputStream is = null;
        try {
            if (getDataFile() != null) {
           	 try {
                	is = new FileInputStream(getDataFile() );
                } catch (IOException e){
                	is = StartupListener.class.getResourceAsStream(getDataFile() );
                }
           	 
           	 if(is != null) {
           		 String dataBuffer = IOUtils.toString(is, Charset.defaultCharset());
           		 LOG.info("刷新内容:\n {}", dataBuffer);

           		 dataJson = new JSONObject(dataBuffer);
           	 }
            } else {
                LOG.error("Please set the config file correctly.");
            }
        } catch (Exception e) {
			LOG.error("data.json read occurred an error, please check content format.");
		} finally {
        	if(is != null) {
        		try {
					is.close();
				} catch (IOException e) {
					;
				}
        	}
        }
    }
    
    public void monitorFileChange() {
		try {
			File file = new File(getDataFile());
			final WatchService watchService = FileSystems.getDefault().newWatchService();

			Path p = Paths.get(file.getParent());
			p.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

			Thread thread = new Thread(new Runnable() {
				public void run() {
					try {
						while (true) {
							WatchKey watchKey = watchService.take();
							List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
							for (WatchEvent<?> event : watchEvents) {
								if(event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
									LOG.info("监听到data文件被修改...");
									refreshData();
								}
							}
							watchKey.reset();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
			thread.setDaemon(false);
			thread.start();

			// 增加jvm关闭的钩子来关闭监听
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

				public void run() {
					try {
						watchService.close();
					} catch (Exception e) {
						;
					}
				}
			}));
		} catch (IOException e) {
			LOG.error("data.json refresh occurred an error, please check content format.");
		}
    }

}
