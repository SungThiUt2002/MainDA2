let isRefreshing = false;
const listeners = new Set();

export const setRefreshing = (value) => {
  isRefreshing = value;
  listeners.forEach((cb) => cb(isRefreshing));
};

export const subscribeRefreshing = (cb) => {
  listeners.add(cb);
  return () => listeners.delete(cb);
};

export const getRefreshing = () => isRefreshing;
