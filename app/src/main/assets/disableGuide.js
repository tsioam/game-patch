(function() {
  'use strict'
  const key = 'clgm_web_app_client_store_config'
  const configData = localStorage.getItem(key)
  const newConfig = Object.assign({}, configData && JSON.parse(configData), { showGameMenuSettingGuide: false })
  localStorage.setItem(key, JSON.stringify(newConfig))
})();