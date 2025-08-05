import React, { useEffect } from "react";
import "./SuccessNotification.css";

const SuccessNotification = ({ message, isVisible, onClose, duration = 3000 }) => {
  useEffect(() => {
    if (isVisible) {
      const timer = setTimeout(() => {
        onClose();
      }, duration);

      return () => clearTimeout(timer);
    }
  }, [isVisible, duration, onClose]);

  if (!isVisible) return null;

  return (
    <div className="success-notification">
      <div className="success-notification-content">
        <div className="success-icon">✅</div>
        <div className="success-message">{message}</div>
        <button className="close-notification" onClick={onClose}>×</button>
      </div>
    </div>
  );
};

export default SuccessNotification; 