function Get-SreWorkspaceAgent {
    param (
        [string] $parmsPath
    )
    
    return [SreWorkspaceAgent]::new($parmsPath)
}

function Out-AgentData {
    param (
        [object] $data
    )

    if ($data.GetType().BaseType -eq [System.Array]) {
        $data | Format-Table;
    }
    else {
        $data
    }

    Write-Output '>>>>begin data>>>>';
    ConvertTo-Json $data;
    Write-Output '<<<<<end data<<<<<<'; 
}

class SreWorkspaceAgent {

    [string]$parmsPath;  
  
    SreWorkspaceAgent([string]$parmsPath) {  
        $this.parmsPath = $parmsPath  
    }
    
    [object] GetInputs() {
        # TODO： 这里只写入了Inputs，上下文也可以写入这里，DEMO 不考虑那么多
        return Get-Content $this.parmsPath | Out-String | ConvertFrom-Json;
    }
}