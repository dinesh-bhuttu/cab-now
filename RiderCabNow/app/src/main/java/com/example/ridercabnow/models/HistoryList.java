package com.example.ridercabnow.models;

import java.util.List;

public class HistoryList {

    private List<History> historyList;

    public HistoryList() {}

    public HistoryList(List<History> historyList) {
        this.historyList = historyList;
    }

    public List<History> getHistoryList() {
        return historyList;
    }

    public void setHistoryList(List<History> historyList) {
        this.historyList = historyList;
    }
}
