<template>
  <div style="width: 100%;">
    <a-input v-model:value="params" placeholder="执行参数" style="width: 100%;" />
  </div>
  <div id="editor" ref="editorRef" style="height: 200px; border: 1px solid black;"></div>
  <div><a-button type="primary" @click="runCommand">运行</a-button></div>
  <div style="width: 100%;">
    <pre><code v-html="code" ref="terminalRef" id="terminal" ></code></pre>
  </div>
</template>  

<script>
import * as monaco from 'monaco-editor/esm/vs/editor/editor.api.js';

import { onMounted, ref } from 'vue';

let terminal = null;

let requestId = null;
let currentLine = 0;
let timerId = null;

class Terminal {

  constructor(container) {
    this.containerElement = container;
  }

  write(message) {
    this.containerElement.textContent += message;
  }
}

let editor = null;
const params = ref('{ "name": "YongMa", "age": 28 }');

const preNewExecute = function () {
  currentLine = 0;
  requestId = null;
  if (timerId) {
    clearTimeout(timerId);
  }
  timerId = null;
}

const pollStdout = function () {
  fetch('http://localhost:8081/api/getStdout?requestId=' + encodeURI(requestId) + "&currentLine=" + encodeURI(currentLine), {
    method: 'GET'
  }).then(response => {
    return response.text();
  }).then(res => {

    res = JSON.parse(res);

    currentLine = res.currentLine;
    terminal.write(res.line);

    if (!res.completed) {
      timerId = setTimeout(pollStdout, 1000);
    }
  }).catch(error => {
    terminal.write(error.message + `\r\n`);
    terminal.write(`# end\r\n`);
  });
};

export default {
  setup() {
    const editorRef = ref(null);
    const terminalRef = ref(null);

    monaco.languages.register({
      id: 'powershell',
      extensions: ['.ps1', '.psm1'],
      aliases: ['PowerShell', 'powershell', 'ps'],
      mimetypes: ['application/x-powershell']
    });

    onMounted(() => {
      editor = monaco.editor.create(editorRef.value, {
        value: '# code goes here \r\nGet-CimInstance -ClassName Win32_Desktop\r\necho $agent.GetInputs()\r\nOut-AgentData (Hostname)',
        language: 'powershell',
        automaticLayout: true
      });

      terminal = new Terminal(terminalRef.value);
    });

    return {
      editorRef,
      terminalRef,
      params
    };
  },
  methods: {
    runCommand() {
      //TODO 开始请求时禁用按钮
      preNewExecute();

      // 获取Monaco编辑器的文本内容  
      const command = editor.getValue();

      //execute command
      fetch('http://localhost:8081/api/executeMultiLine?params=' + encodeURI(params.value), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: command
      }).then(response => {
        return response.text();
      }).then(output => {
        requestId = output;
        timerId = setTimeout(pollStdout, 100);
      }).catch(error => {
        terminal.write(error.message + `\r\n`);
        terminal.write(`# end\r\n`);
      });
    }
  }
}

</script>
<style>   code {
     background-color: #1c1e22;
     color: #fff;
     font-family: Consolas, "Courier New", monospace;
     font-size: 14px;
     padding: 10px;
     line-height: 1.5;
     display: block;
     overflow-x: auto;
     white-space: pre-wrap;
     border-radius: 5px;
     height: 280px;
   }
</style>  