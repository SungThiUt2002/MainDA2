import React, { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import { jwtDecode } from "jwt-decode";
import { loginUser } from "../../api/authApi";
import "./LoginForm.css";

// Validation functions
const validateEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

const validateUsername = (username) => {
  return username.length >= 3;
};

const validatePassword = (password) => {
  return password.length >= 6;
};

const LoginForm = () => {
  const [formData, setFormData] = useState({ usernameOrEmail: "", password: "" });
  const [rememberMe, setRememberMe] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [validationErrors, setValidationErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const savedUser = JSON.parse(localStorage.getItem("rememberUser"));
    if (savedUser) {
      setFormData(savedUser);
      setRememberMe(true);
    }
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
    
    // Clear validation error when user starts typing
    if (validationErrors[name]) {
      setValidationErrors(prev => ({
        ...prev,
        [name]: ""
      }));
    }
    
    // Clear general error when user starts typing
    if (errorMsg) {
      setErrorMsg("");
    }
  };

  const validateForm = () => {
    const errors = {};
    const { usernameOrEmail, password } = formData;

    // Validate username/email
    if (!usernameOrEmail.trim()) {
      errors.usernameOrEmail = "Vui lòng nhập tên đăng nhập hoặc email";
    } else if (usernameOrEmail.includes("@")) {
      if (!validateEmail(usernameOrEmail)) {
        errors.usernameOrEmail = "Email không hợp lệ";
      }
    } else {
      if (!validateUsername(usernameOrEmail)) {
        errors.usernameOrEmail = "Tên đăng nhập phải có ít nhất 3 ký tự";
      }
    }

    // Validate password
    if (!password) {
      errors.password = "Vui lòng nhập mật khẩu";
    } else if (!validatePassword(password)) {
      errors.password = "Mật khẩu phải có ít nhất 6 ký tự";
    }

    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validate form before submission
    if (!validateForm()) {
      return;
    }

    setErrorMsg("");
    setLoading(true);
    setIsSubmitting(true);

    try {
      const data = await loginUser(formData);
      const { accessToken, refreshToken } = data;
      const decoded = jwtDecode(accessToken);
      const roles = decoded.roles || [];

      localStorage.setItem("accessToken", accessToken);
      localStorage.setItem("refreshToken", refreshToken);
      
      rememberMe
        ? localStorage.setItem("rememberUser", JSON.stringify(formData))
        : localStorage.removeItem("rememberUser");

      // Show success message briefly before redirect
      setErrorMsg(""); // Clear any previous errors
      
      if (roles.includes("ADMIN")) {
        setTimeout(() => navigate("/admin/dashboard"), 500);
      } else if (roles.includes("USER")) {
        setTimeout(() => navigate("/"), 500);
      } else {
        setErrorMsg("Không xác định được vai trò.");
      }
    } catch (err) {
      const msg = err?.response?.data?.message || "Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.";
      setErrorMsg(msg);
    } finally {
      setLoading(false);
      setIsSubmitting(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-background">
        <div className="login-background-overlay"></div>
      </div>
      
      <form className="login-form" onSubmit={handleSubmit}>
        <div className="login-header">
          <div className="login-logo">🛍️</div>
          <h2>Chào mừng trở lại!</h2>
          <p>Đăng nhập để tiếp tục mua sắm</p>
        </div>

        <div className="form-group">
          <label htmlFor="usernameOrEmail">Tên đăng nhập hoặc Email</label>
          <div className="input-wrapper">
            <input
              type="text"
              id="usernameOrEmail"
              name="usernameOrEmail"
              placeholder="Nhập tên đăng nhập hoặc email"
              value={formData.usernameOrEmail}
              onChange={handleChange}
              className={validationErrors.usernameOrEmail ? "error" : ""}
            />
            {validationErrors.usernameOrEmail && (
              <span className="error-icon">⚠️</span>
            )}
          </div>
          {validationErrors.usernameOrEmail && (
            <span className="field-error">{validationErrors.usernameOrEmail}</span>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="password">Mật khẩu</label>
          <div className="input-wrapper">
            <input
              type={showPassword ? "text" : "password"}
              id="password"
              name="password"
              placeholder="Nhập mật khẩu"
              value={formData.password}
              onChange={handleChange}
              className={validationErrors.password ? "error" : ""}
            />
            <button
              type="button"
              className="password-toggle"
              onClick={() => setShowPassword(!showPassword)}
            >
              {showPassword ? "🙈" : "👁️"}
            </button>
            {validationErrors.password && (
              <span className="error-icon">⚠️</span>
            )}
          </div>
          {validationErrors.password && (
            <span className="field-error">{validationErrors.password}</span>
          )}
        </div>

        <div className="login-options">
          <label className="checkbox-wrapper">
            <input
              type="checkbox"
              checked={rememberMe}
              onChange={() => setRememberMe(!rememberMe)}
            />
            <span className="checkmark"></span>
            Ghi nhớ đăng nhập
          </label>
          <Link to="/forgot-password" className="forgot-link">Quên mật khẩu?</Link>
        </div>

        {errorMsg && (
          <div className="error-message">
            <span className="error-icon">❌</span>
            {errorMsg}
          </div>
        )}

        <button 
          type="submit" 
          disabled={loading || isSubmitting}
          className={loading ? "loading" : ""}
        >
          {loading ? (
            <>
              <span className="spinner"></span>
              Đang xử lý...
            </>
          ) : (
            "Đăng nhập"
          )}
        </button>

        <div className="signup-link">
          <span>Chưa có tài khoản?</span>
          <Link to="/register" className="signup-btn">Đăng ký ngay</Link>
        </div>

        <div className="back-to-home">
          <Link to="/" className="back-link">
            ← Quay về trang chủ
          </Link>
        </div>
      </form>
    </div>
  );
};

export default LoginForm;
