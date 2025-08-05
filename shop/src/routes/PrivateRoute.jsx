// src/routes/PrivateRoute.jsx
import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import { jwtDecode } from "jwt-decode";

const PrivateRoute = ({ allowedRoles }) => {
  const token = localStorage.getItem("accessToken");

  if (!token) return <Navigate to="/login" replace />;

  try {
    const decoded = jwtDecode(token);
    const roles = decoded.roles || [];

    const isAllowed = roles.some((role) => allowedRoles.includes(role));
    return isAllowed ? <Outlet /> : <Navigate to="/unauthorized" replace />;
  } catch (err) {
    return <Navigate to="/login" replace />;
  }
};

export default PrivateRoute;
