export function nonce8() {
  const nonce = Math.random().toString(16).slice(2, 8);
  if (nonce.length < 8) {
    return (`${nonce}xxxxxxxx`).slice(0, 8);
  }
  return nonce;
}
