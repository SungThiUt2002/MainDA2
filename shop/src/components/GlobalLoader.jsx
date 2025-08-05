// src/components/GlobalLoader.jsx
import React, { useEffect, useState } from "react";
import { subscribeRefreshing, getRefreshing } from "../store/loadingStore";

const GlobalLoader = () => {
  const [show, setShow] = useState(getRefreshing());

  useEffect(() => {
    const unsubscribe = subscribeRefreshing(setShow);
    return () => unsubscribe();
  }, []);

  if (!show) return null;

  return (
    <div style={{
      position: "fixed",
      top: 0,
      left: 0,
      width: "100%",
      backgroundColor: "#ffd700",
      color: "#000",
      textAlign: "center",
      padding: "10px",
      zIndex: 9999,
    }}>
      🔁 Đang làm mới phiên làm việc...
    </div>
  );
};

export default GlobalLoader;
