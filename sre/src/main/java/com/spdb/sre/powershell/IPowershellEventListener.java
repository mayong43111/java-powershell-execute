package com.spdb.sre.powershell;

public interface IPowershellEventListener {

    void handleOutputLine(String stdout);

    void handleReturnData(String data);
}
