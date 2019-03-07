package org.demo.pool;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

public class TestWatchService {

	public static void main(String[] args) throws IOException {
		// 需要监听的文件目录（只能监听目录）
		final String path = "d:/test";
		final WatchService watchService = FileSystems.getDefault().newWatchService();
		Path p = Paths.get(path);
		p.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

		Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						WatchKey watchKey = watchService.take();
						List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
						for (WatchEvent<?> event : watchEvents) {
							// TODO 根据事件类型采取不同的操作。。。。。。。
							System.out.println("[" + path + "/" + event.context() + "]文件发生了[" + event.kind() + "]事件");
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
	}
}
