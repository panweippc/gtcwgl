// 通用格式化工具

/** 格式化金额(保留两位小数) */
export function formatMoney(v) {
  if (v === null || v === undefined || v === '') return '0.00'
  return Number(v).toFixed(2)
}

/** 格式化日期 yyyy-MM-dd */
export function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  const pad = n => (n < 10 ? '0' + n : '' + n)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/** 格式化日期时间 yyyy-MM-dd HH:mm:ss */
export function formatDateTime(date) {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  const pad = n => (n < 10 ? '0' + n : '' + n)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}
