/**
 * Chrome Extension 消息处理脚本
 * 
 * 解决 Chrome 扩展干扰导致的异步消息错误
 * Error: A listener indicated an asynchronous response by returning true,
 *        but the message channel closed before a response was received
 */

(function() {
  'use strict';

  // 检查是否在浏览器环境中
  if (typeof window === 'undefined') {
    return;
  }

  /**
   * 初始化 Chrome Extension 消息处理
   */
  function initExtensionHandler() {
    // 检查 chrome.runtime 是否可用
    if (typeof chrome === 'undefined' || !chrome.runtime) {
      console.debug('[Extension Handler] Chrome runtime not available');
      return;
    }

    try {
      // 设置消息监听器
      chrome.runtime.onMessage.addListener(function(request, sender, sendResponse) {
        try {
          console.debug('[Extension Handler] Message received from extension:', request);

          // 确保异步响应被正确处理
          if (sendResponse && typeof sendResponse === 'function') {
            sendResponse({ status: 'received', message: 'Message processed' });
          }

          // 返回 true 表示将异步处理响应
          return true;
        } catch (error) {
          console.warn('[Extension Handler] Error handling message:', error);

          // 尝试发送错误响应
          try {
            if (sendResponse && typeof sendResponse === 'function') {
              sendResponse({ status: 'error', message: error.message });
            }
          } catch (e) {
            console.warn('[Extension Handler] Could not send error response:', e);
          }

          return false;
        }
      });

      console.info('[Extension Handler] Message listener initialized successfully');
    } catch (error) {
      console.warn('[Extension Handler] Failed to initialize message listener:', error);
    }
  }

  /**
   * 安全包装 chrome.runtime 调用
   */
  function safeExtensionCall(callback) {
    if (typeof chrome === 'undefined' || !chrome.runtime) {
      return;
    }

    try {
      callback();
    } catch (error) {
      console.warn('[Extension Handler] Extension call failed:', error);
    }
  }

  /**
   * 发送消息到 Extension (带超时保护)
   */
  window.sendToExtension = function(message, timeout = 2000) {
    return new Promise((resolve, reject) => {
      if (typeof chrome === 'undefined' || !chrome.runtime) {
        reject(new Error('Chrome runtime not available'));
        return;
      }

      const timeoutId = setTimeout(() => {
        reject(new Error('Extension message timeout'));
      }, timeout);

      try {
        chrome.runtime.sendMessage(message, function(response) {
          clearTimeout(timeoutId);

          // 检查 chrome.runtime.lastError
          if (chrome.runtime.lastError) {
            reject(new Error(chrome.runtime.lastError.message));
          } else {
            resolve(response);
          }
        });
      } catch (error) {
        clearTimeout(timeoutId);
        reject(error);
      }
    });
  };

  /**
   * 捕获未处理的 Extension 相关错误
   */
  window.addEventListener('error', function(event) {
    if (event.message && event.message.includes('message channel closed')) {
      console.warn('[Extension Handler] Extension channel closed, suppressing error');
      event.preventDefault();
    }
  });

  // 页面加载完成后初始化
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initExtensionHandler);
  } else {
    initExtensionHandler();
  }

  // 导出处理函数供全局使用
  window.ExtensionHandler = {
    init: initExtensionHandler,
    safeCall: safeExtensionCall,
    sendMessage: window.sendToExtension
  };

  console.info('[Extension Handler] Module loaded and ready');
})();
