<template>
  <pre><code v-html="code"></code></pre>
</template>

<script>
export default {
  data() {
    return {
      code: `  
Pinging 127.0.0.1 with 32 bytes of data:
Reply from 127.0.0.1: bytes=32 time<1ms TTL=128
Reply from 127.0.0.1: bytes=32 time<1ms TTL=128
Reply from 127.0.0.1: bytes=32 time<1ms TTL=128
Reply from 127.0.0.1: bytes=32 time<1ms TTL=128

Ping statistics for 127.0.0.1:
    Packets: Sent = 4, Received = 4, Lost = 0 (0% loss),
Approximate round trip times in milli-seconds:
    Minimum = 0ms, Maximum = 0ms, Average = 0ms
      `,
    };
  },
};
</script>

<style>
code {
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
}
</style>
