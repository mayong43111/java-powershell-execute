package com.spdb.sre.powershell;

public abstract class AbstractPowershellEventListener implements IPowershellEventListener {

    @Override
    public void handleOutputLine(String stdout) {
    }

    @Override
    public void handleReturnData(String data) {
    }

    @Override
    public void handleCompleted(int exitCode) {
    }
}
