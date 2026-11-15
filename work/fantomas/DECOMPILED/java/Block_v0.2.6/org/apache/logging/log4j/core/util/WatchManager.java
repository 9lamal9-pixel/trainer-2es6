/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.config.ConfigurationFileWatcher;
import org.apache.logging.log4j.core.config.ConfigurationScheduler;
import org.apache.logging.log4j.core.util.FileWatcher;
import org.apache.logging.log4j.core.util.Source;
import org.apache.logging.log4j.core.util.UuidUtil;
import org.apache.logging.log4j.core.util.WatchEventService;
import org.apache.logging.log4j.core.util.Watcher;
import org.apache.logging.log4j.core.util.WrappedFileWatcher;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.LoaderUtil;

public class WatchManager
extends AbstractLifeCycle {
    private static Logger logger = StatusLogger.getLogger();
    private final ConcurrentMap<Source, ConfigurationMonitor> watchers = new ConcurrentHashMap<Source, ConfigurationMonitor>();
    private int intervalSeconds = 0;
    private ScheduledFuture<?> future;
    private final ConfigurationScheduler scheduler;
    private final List<WatchEventService> eventServiceList;
    private final UUID id = UuidUtil.getTimeBasedUuid();

    public WatchManager(ConfigurationScheduler scheduler) {
        this.scheduler = scheduler;
        this.eventServiceList = this.getEventServices();
    }

    public UUID getId() {
        return this.id;
    }

    public boolean hasEventListeners() {
        return this.eventServiceList.size() > 0;
    }

    public void reset() {
        logger.debug("Resetting {}", (Object)this);
        for (Source source : this.watchers.keySet()) {
            this.reset(source);
        }
    }

    public void reset(File file) {
        if (file == null) {
            return;
        }
        Source source = new Source(file);
        this.reset(source);
    }

    public void reset(Source source) {
        Watcher watcher;
        if (source == null) {
            return;
        }
        ConfigurationMonitor monitor = (ConfigurationMonitor)this.watchers.get(source);
        if (monitor != null && (watcher = monitor.getWatcher()).isModified()) {
            long lastModifiedMillis = watcher.getLastModified();
            if (logger.isDebugEnabled()) {
                logger.debug("Resetting file monitor for '{}' from {} ({}) to {} ({})", (Object)source.getLocation(), (Object)this.millisToString(monitor.lastModifiedMillis), (Object)monitor.lastModifiedMillis, (Object)this.millisToString(lastModifiedMillis), (Object)lastModifiedMillis);
            }
            monitor.setLastModifiedMillis(lastModifiedMillis);
        }
    }

    public void setIntervalSeconds(int intervalSeconds) {
        if (!this.isStarted()) {
            if (this.intervalSeconds > 0 && intervalSeconds == 0) {
                this.scheduler.decrementScheduledItems();
            } else if (this.intervalSeconds == 0 && intervalSeconds > 0) {
                this.scheduler.incrementScheduledItems();
            }
            this.intervalSeconds = intervalSeconds;
        }
    }

    public int getIntervalSeconds() {
        return this.intervalSeconds;
    }

    @Override
    public void start() {
        super.start();
        if (this.intervalSeconds > 0) {
            this.future = this.scheduler.scheduleWithFixedDelay(new WatchRunnable(), this.intervalSeconds, this.intervalSeconds, TimeUnit.SECONDS);
        }
        for (WatchEventService service : this.eventServiceList) {
            service.subscribe(this);
        }
    }

    @Override
    public boolean stop(long timeout, TimeUnit timeUnit) {
        this.setStopping();
        for (WatchEventService service : this.eventServiceList) {
            service.unsubscribe(this);
        }
        boolean stopped = this.stop(this.future);
        this.setStopped();
        return stopped;
    }

    public void unwatchFile(File file) {
        Source source = new Source(file);
        this.unwatch(source);
    }

    public void unwatch(Source source) {
        logger.debug("Unwatching configuration {}", (Object)source);
        this.watchers.remove(source);
    }

    public void checkFiles() {
        new WatchRunnable().run();
    }

    public void watchFile(File file, FileWatcher fileWatcher) {
        Watcher watcher = fileWatcher instanceof Watcher ? (Watcher)((Object)fileWatcher) : new WrappedFileWatcher(fileWatcher);
        Source source = new Source(file);
        this.watch(source, watcher);
    }

    public void watch(Source source, Watcher watcher) {
        watcher.watching(source);
        long lastModified = watcher.getLastModified();
        if (logger.isDebugEnabled()) {
            logger.debug("Watching configuration '{}' for lastModified {} ({})", (Object)source, (Object)this.millisToString(lastModified), (Object)lastModified);
        }
        this.watchers.put(source, new ConfigurationMonitor(lastModified, watcher));
    }

    public Map<File, FileWatcher> getWatchers() {
        HashMap<File, FileWatcher> map = new HashMap<File, FileWatcher>(this.watchers.size());
        for (Map.Entry entry : this.watchers.entrySet()) {
            if (((ConfigurationMonitor)entry.getValue()).getWatcher() instanceof ConfigurationFileWatcher) {
                map.put(((Source)entry.getKey()).getFile(), (FileWatcher)((Object)((ConfigurationMonitor)entry.getValue()).getWatcher()));
                continue;
            }
            map.put(((Source)entry.getKey()).getFile(), new WrappedFileWatcher((FileWatcher)((Object)((ConfigurationMonitor)entry.getValue()).getWatcher())));
        }
        return map;
    }

    public Map<Source, Watcher> getConfigurationWatchers() {
        HashMap<Source, Watcher> map = new HashMap<Source, Watcher>(this.watchers.size());
        for (Map.Entry entry : this.watchers.entrySet()) {
            map.put((Source)entry.getKey(), ((ConfigurationMonitor)entry.getValue()).getWatcher());
        }
        return map;
    }

    private String millisToString(long millis) {
        return new Date(millis).toString();
    }

    private List<WatchEventService> getEventServices() {
        ArrayList<WatchEventService> list = new ArrayList<WatchEventService>();
        for (ClassLoader classLoader : LoaderUtil.getClassLoaders()) {
            try {
                ServiceLoader<WatchEventService> serviceLoader = ServiceLoader.load(WatchEventService.class, classLoader);
                for (WatchEventService service : serviceLoader) {
                    list.add(service);
                }
            }
            catch (Throwable ex) {
                LOGGER.debug("Unable to retrieve WatchEventService from ClassLoader {}", (Object)classLoader, (Object)ex);
            }
        }
        return list;
    }

    public String toString() {
        return "WatchManager [intervalSeconds=" + this.intervalSeconds + ", watchers=" + this.watchers + ", scheduler=" + this.scheduler + ", future=" + this.future + "]";
    }

    private final class ConfigurationMonitor {
        private final Watcher watcher;
        private volatile long lastModifiedMillis;

        public Watcher getWatcher() {
            return this.watcher;
        }

        public ConfigurationMonitor(long lastModifiedMillis, Watcher watcher) {
            this.watcher = watcher;
            this.lastModifiedMillis = lastModifiedMillis;
        }

        private void setLastModifiedMillis(long lastModifiedMillis) {
            this.lastModifiedMillis = lastModifiedMillis;
        }

        public String toString() {
            return "ConfigurationMonitor [watcher=" + this.watcher + ", lastModifiedMillis=" + this.lastModifiedMillis + "]";
        }
    }

    private final class WatchRunnable
    implements Runnable {
        private final String SIMPLE_NAME = WatchRunnable.class.getSimpleName();

        private WatchRunnable() {
        }

        @Override
        public void run() {
            logger.trace("{} run triggered.", (Object)this.SIMPLE_NAME);
            for (Map.Entry entry : WatchManager.this.watchers.entrySet()) {
                Source source = (Source)entry.getKey();
                ConfigurationMonitor monitor = (ConfigurationMonitor)entry.getValue();
                if (!monitor.getWatcher().isModified()) continue;
                long lastModified = monitor.getWatcher().getLastModified();
                if (logger.isInfoEnabled()) {
                    logger.info("Source '{}' was modified on {} ({}), previous modification was on {} ({})", (Object)source, (Object)WatchManager.this.millisToString(lastModified), (Object)lastModified, (Object)WatchManager.this.millisToString(monitor.lastModifiedMillis), (Object)monitor.lastModifiedMillis);
                }
                monitor.lastModifiedMillis = lastModified;
                monitor.getWatcher().modified();
            }
            logger.trace("{} run ended.", (Object)this.SIMPLE_NAME);
        }
    }
}

