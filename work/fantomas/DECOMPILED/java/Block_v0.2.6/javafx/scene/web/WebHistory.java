/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.web;

import com.sun.webkit.BackForwardList;
import com.sun.webkit.WebPage;
import java.net.URL;
import java.util.Date;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class WebHistory {
    private final BackForwardList bfl;
    private final ObservableList<Entry> list;
    private final ObservableList<Entry> ulist;
    private final ReadOnlyIntegerWrapper currentIndex = new ReadOnlyIntegerWrapper(this, "currentIndex");
    private IntegerProperty maxSize;

    WebHistory(WebPage webPage) {
        this.list = FXCollections.observableArrayList();
        this.ulist = FXCollections.unmodifiableObservableList(this.list);
        this.bfl = webPage.createBackForwardList();
        this.setMaxSize(this.getMaxSize());
        this.bfl.addChangeListener(wCChangeEvent -> {
            if (this.bfl.size() > this.list.size()) {
                assert (this.bfl.size() == this.list.size() + 1);
                this.list.add(new Entry(this.bfl.getCurrentEntry()));
                this.setCurrentIndex(this.list.size() - 1);
                return;
            }
            if (this.bfl.size() == this.list.size()) {
                if (this.list.size() == 0) {
                    return;
                }
                assert (this.list.size() > 0);
                BackForwardList.Entry entry = this.bfl.get(this.list.size() - 1);
                BackForwardList.Entry entry2 = this.bfl.get(0);
                if (((Entry)this.list.get(this.list.size() - 1)).isPeer(entry)) {
                    this.setCurrentIndex(this.bfl.getCurrentIndex());
                    return;
                }
                if (!((Entry)this.list.get(0)).isPeer(entry2)) {
                    this.list.remove(0);
                    this.list.add(new Entry(entry));
                    this.setCurrentIndex(this.bfl.getCurrentIndex());
                    return;
                }
            }
            assert (this.bfl.size() <= this.list.size());
            this.list.remove(this.bfl.size(), this.list.size());
            int n = this.list.size() - 1;
            if (n >= 0 && !((Entry)this.list.get(n)).isPeer(this.bfl.get(n))) {
                this.list.remove(n);
                this.list.add(new Entry(this.bfl.get(n)));
            }
            this.setCurrentIndex(this.bfl.getCurrentIndex());
        });
    }

    public ReadOnlyIntegerProperty currentIndexProperty() {
        return this.currentIndex.getReadOnlyProperty();
    }

    public int getCurrentIndex() {
        return this.currentIndexProperty().get();
    }

    private void setCurrentIndex(int n) {
        this.currentIndex.set(n);
    }

    public IntegerProperty maxSizeProperty() {
        if (this.maxSize == null) {
            this.maxSize = new SimpleIntegerProperty(this, "maxSize", 100){

                @Override
                public void set(int n) {
                    if (n < 0) {
                        throw new IllegalArgumentException("value cannot be negative.");
                    }
                    super.set(n);
                }
            };
        }
        return this.maxSize;
    }

    public void setMaxSize(int n) {
        this.maxSizeProperty().set(n);
        this.bfl.setMaximumSize(n);
    }

    public int getMaxSize() {
        return this.maxSizeProperty().get();
    }

    public ObservableList<Entry> getEntries() {
        return this.ulist;
    }

    public void go(int n) throws IndexOutOfBoundsException {
        if (n == 0) {
            return;
        }
        int n2 = this.getCurrentIndex() + n;
        if (n2 < 0 || n2 >= this.list.size()) {
            throw new IndexOutOfBoundsException("the effective index " + n2 + " is out of the range [0.." + (this.list.size() - 1) + "]");
        }
        this.bfl.setCurrentIndex(n2);
    }

    public final class Entry {
        private final URL url;
        private final ReadOnlyObjectWrapper<String> title = new ReadOnlyObjectWrapper(this, "title");
        private final ReadOnlyObjectWrapper<Date> lastVisitedDate = new ReadOnlyObjectWrapper(this, "lastVisitedDate");
        private final BackForwardList.Entry peer;

        private Entry(BackForwardList.Entry entry) {
            this.url = entry.getURL();
            this.title.set(entry.getTitle());
            this.lastVisitedDate.set(entry.getLastVisitedDate());
            this.peer = entry;
            entry.addChangeListener(wCChangeEvent -> {
                Date date;
                String string = entry.getTitle();
                if (string == null || !string.equals(this.getTitle())) {
                    this.title.set(string);
                }
                if ((date = entry.getLastVisitedDate()) != null && !date.equals(this.getLastVisitedDate())) {
                    this.lastVisitedDate.set(date);
                }
            });
        }

        public String getUrl() {
            assert (this.url != null);
            return this.url.toString();
        }

        public ReadOnlyObjectProperty<String> titleProperty() {
            return this.title.getReadOnlyProperty();
        }

        public String getTitle() {
            return (String)this.title.get();
        }

        public ReadOnlyObjectProperty<Date> lastVisitedDateProperty() {
            return this.lastVisitedDate.getReadOnlyProperty();
        }

        public Date getLastVisitedDate() {
            return (Date)this.lastVisitedDate.get();
        }

        boolean isPeer(BackForwardList.Entry entry) {
            return this.peer == entry;
        }

        public String toString() {
            return "[url: " + this.getUrl() + ", title: " + this.getTitle() + ", date: " + this.getLastVisitedDate() + "]";
        }
    }
}

