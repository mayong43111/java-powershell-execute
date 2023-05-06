# code goes here 
Get-CimInstance -ClassName Win32_Desktop
echo $agent.GetInputs()
Out-AgentData (Hostname)