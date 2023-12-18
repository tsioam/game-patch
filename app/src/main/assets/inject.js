(function () {

  if (typeof window['CG_BRIDGE']?.['evalMethod'] !== 'function') {
    return
  }
  const bridgeFunction = window['CG_BRIDGE']['evalMethod'].bind(window['CG_BRIDGE'])

  const callbackMap = new Map()
  const id2eleMap = new Map()
  const ele2IdMap = new Map()
  const newId = (function () {
    let count = 0;
    return function () {
      return String(count++);
    };
  })()
  const lockState = {
    isLocked: false,
    lockElement: null
  }

  const getElementId = (ele) => {
    if (ele2IdMap.get(ele)) {
      return ele2IdMap.get(ele)
    }
    const id = newId()
    ele2IdMap.set(ele, id)
    id2eleMap.set(id, ele)
    return id
  }

  const getEleById = (id) => {
    return id2eleMap.get(id)
  }


  window.CG_CALLBACKS = callbackMap
  window.CG_EVAL_CALLBACK = function (callbackId, result) {
    const callback = callbackMap.get(callbackId)
    if (typeof callback === 'function') {
      callback(result)
    }
    callbackMap.delete(callbackId)
  }
  // params should be string
  const evalBridgeMethodWithCallback = function (method, params, callback) {
    const callbackId = newId()
    callbackMap.set(callbackId, callback)
    bridgeFunction(method, params || '', callbackId)
  }
  const evalBridgeMethodPromise = function (method, params) {
    return new Promise((resolve, reject) => {
      evalBridgeMethodWithCallback(method, params, resolve)
    })
  }
  const justEvalBridgeMethod = function (method, params) {
    bridgeFunction(method, params || '', '')
  }

  const pointerlockchangeListener = new Set()
  HTMLElement.prototype.requestPointerLock = function (/* ignore */) {
    const eleId = getElementId(this)
    return evalBridgeMethodPromise('requestPointerLock', eleId).then(() => {
    })
  }

  Object.defineProperty(document, 'exitPointerLock', {
    value: () => {
      evalBridgeMethodPromise('exitPointerLock').then(() => { })
    },
    writable: true
  })

  const mouseMoveMap = new Map()
  const mousemoveDocumentListener = new Set()
  const htmlElementAddEventListener = HTMLElement.prototype.addEventListener
  HTMLElement.prototype.addEventListener = function (...args) {
    if (args[0] === 'mousemove') {
      const callback = args[1]
      mouseMoveMap.set(this, callback)
    }
    return htmlElementAddEventListener.apply(this, args)
  }

  const addEventListener = document.addEventListener
  document.addEventListener = function (...args) {
    if (args[0] === 'pointerlockchange') {
      pointerlockchangeListener.add(args[1])
      return
    }
    if (args[0] === 'mousemove' && typeof args[1] === 'function') {
      mousemoveDocumentListener.add(args[1])
    }
    return addEventListener.apply(document, args)
  }

  const removeEventListener = document.removeEventListener
  document.removeEventListener = function (...args) {
    if (args[0] === 'pointerlockchange') {
      pointerlockchangeListener.delete(args[1])
      return
    }
    return removeEventListener.apply(document, args)
  }

  Object.defineProperty(document, 'pointerLockElement', {
    get: () => lockState.lockElement
  })
  window.POINTER_LOCK_CHANGE_CB = (hasCapture, eleId) => {
    try {
      const ele = getEleById(eleId)
      lockState.isLocked = hasCapture
      lockState.lockElement = hasCapture ? ele : null
      for (const listener of pointerlockchangeListener) {
        listener()
      }
    } catch (e) {
      console.error(e)
    }

  }

  window.EVAL_MOVEMENT_CB = (x, y) => {
    try {
      let eventEle = lockState.lockElement
      const callback = mouseMoveMap.get(eventEle)
      if (typeof callback === 'function') {
        callback(new MouseEvent('mousemove', {
          movementX: x,
          movementY: y
        }))
      }
      for (const cb of mousemoveDocumentListener) {
        cb(new MouseEvent('mousemove', {
          movementX: x,
          movementY: y
        }))
      }
    } catch (e) {
      console.error(e)
    }
  }

  console.log('inject.js loaded')

})()