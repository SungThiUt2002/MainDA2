import React, { useEffect, useState } from "react";
import {
  getAllRoles,
  createRole,
  updateRole,
  deleteRole,
  getAllPermissions,
  createPermission,
  updatePermission,
  deletePermission,
} from "../../api/roleApi";
import "./RolePermissionManager.css";

const RolePermissionManager = () => {
  const [activeTab, setActiveTab] = useState("roles");
  const [roles, setRoles] = useState([]);
  const [permissions, setPermissions] = useState([]);
  const [roleForm, setRoleForm] = useState({ name: "", description: "", permissions: [] });
  const [permissionForm, setPermissionForm] = useState({ name: "", description: "" });
  const [editingRole, setEditingRole] = useState(null);
  const [editingPermission, setEditingPermission] = useState(null);
  const [error, setError] = useState("");

  const token = localStorage.getItem("accessToken");

  // Fetch roles & permissions
  useEffect(() => {
    fetchAll();
    // eslint-disable-next-line
  }, [activeTab]);

  const fetchAll = async () => {
    try {
      const [roleRes, permRes] = await Promise.all([
        getAllRoles(token),
        getAllPermissions(token),
      ]);
      setRoles(roleRes.data.data || []);
    //   setPermissions(permRes.data.data || []);
      setPermissions(
        Array.isArray(permRes.data) ? permRes.data :
        Array.isArray(permRes.data.data) ? permRes.data.data : []
    );
    } catch (err) {
      setError("Không thể tải dữ liệu role/permission");
    }
  };

  // Role handlers
  const handleRoleFormChange = (e) => {
    const { name, value } = e.target;
    setRoleForm({ ...roleForm, [name]: value });
  };

//   const handleRolePermissionChange = (permId) => {
//     setRoleForm((prev) => ({
//       ...prev,
//       permissions: prev.permissions.includes(permId)
//         ? prev.permissions.filter((p) => p !== permId)
//         : [...prev.permissions, permId],
//     }));
//   };
    const handleRolePermissionChange = (permName) => {
    setRoleForm((prev) => ({
        ...prev,
        permissions: prev.permissions.includes(permName)
        ? prev.permissions.filter((p) => p !== permName)
        : [...prev.permissions, permName],
    }));
    };

  const handleAddOrUpdateRole = async (e) => {
  e.preventDefault();
  const payload = {
    name: roleForm.name,
    description: roleForm.description,
    permissionName: roleForm.permissions, // phải là mảng tên permission
  };
  try {
    if (editingRole) {
      await updateRole(editingRole.id, payload, token);
    } else {
      await createRole(payload, token);
    }
    setRoleForm({ name: "", description: "", permissions: [] });
    setEditingRole(null);
    fetchAll();
  } catch (err) {
    setError("Lưu role thất bại!");
  }
};

  const handleEditRole = (role) => {
    setEditingRole(role);
    setRoleForm({
      name: role.name,
      description: role.description,
      permissions: role.permissions ? role.permissions.map((p) => p.id) : [],
    });
  };

  const handleDeleteRole = async (id) => {
    if (!window.confirm("Bạn có chắc muốn xóa role này?")) return;
    try {
      await deleteRole(id, token);
      fetchAll();
    } catch {
      setError("Xóa role thất bại!");
    }
  };

  // Permission handlers
  const handlePermissionFormChange = (e) => {
    const { name, value } = e.target;
    setPermissionForm({ ...permissionForm, [name]: value });
  };

  const handleAddOrUpdatePermission = async (e) => {
    e.preventDefault();
    try {
      if (editingPermission) {
        await updatePermission(editingPermission.id, permissionForm, token);
      } else {
        await createPermission(permissionForm, token);
      }
      setPermissionForm({ name: "", description: "" });
      setEditingPermission(null);
      fetchAll();
    } catch {
      setError("Lưu permission thất bại!");
    }
  };

  const handleEditPermission = (perm) => {
    setEditingPermission(perm);
    setPermissionForm({ name: perm.name, description: perm.description });
  };

  const handleDeletePermission = async (id) => {
    if (!window.confirm("Bạn có chắc muốn xóa permission này?")) return;
    try {
      await deletePermission(id, token);
      fetchAll();
    } catch {
      setError("Xóa permission thất bại!");
    }
  };

  return (
    <div className="role-permission-manager">
      <div className="tab-header">
        <button onClick={() => setActiveTab("roles")} className={activeTab === "roles" ? "active" : ""}>
          Quản lý Role
        </button>
        <button onClick={() => setActiveTab("permissions")} className={activeTab === "permissions" ? "active" : ""}>
          Quản lý Permission
        </button>
      </div>
      {error && <div className="error-message">{error}</div>}

      {activeTab === "roles" && (
        <div>
          <h3>Danh sách Role ({roles.length})</h3>
          <table className="role-table">
            <thead>
              <tr>
                <th>Tên Role</th>
                <th>Mô tả</th>
                <th>Permissions</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {roles.map((role) => (
                <tr key={role.id}>
                  <td>{role.name}</td>
                  <td>{role.description}</td>
                  <td>
                    {role.permissions && role.permissions.length > 0
                      ? role.permissions.map((p) => p.name).join(", ")
                      : "Không có quyền"}
                  </td>
                  <td>
                    <button onClick={() => handleEditRole(role)}>Sửa</button>
                    <button onClick={() => handleDeleteRole(role.id)}>Xóa</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <h4>{editingRole ? "Sửa Role" : "Thêm Role mới"}</h4>
          <form className="role-form" onSubmit={handleAddOrUpdateRole}>
            <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
              <input 
                name="name" 
                placeholder="Tên role (VD: ADMIN, USER)" 
                value={roleForm.name} 
                onChange={handleRoleFormChange} 
                required 
              />
              <input 
                name="description" 
                placeholder="Mô tả role" 
                value={roleForm.description} 
                onChange={handleRoleFormChange} 
              />
            </div>
            <div>
              <span>Chọn quyền ({roleForm.permissions.length} đã chọn):</span>
              <div style={{ marginTop: '0.5rem' }}>
                {permissions.map((perm) => (
                  <label key={perm.id}>
                    <input
                      type="checkbox"
                      checked={roleForm.permissions.includes(perm.name)}
                      onChange={() => handleRolePermissionChange(perm.name)}
                    />
                    <span>{perm.name}</span>
                  </label>
                ))}
              </div>
            </div>
            <div style={{ marginTop: '1rem' }}>
              <button type="submit">{editingRole ? "Cập nhật Role" : "Thêm Role"}</button>
              {editingRole && (
                <button 
                  type="button" 
                  onClick={() => { 
                    setEditingRole(null); 
                    setRoleForm({ name: "", description: "", permissions: [] }); 
                  }}
                >
                  Hủy
                </button>
              )}
            </div>
          </form>
        </div>
      )}

      {activeTab === "permissions" && (
        <div>
          <h3>Danh sách Permission ({permissions.length})</h3>
          <table className="permission-table">
            <thead>
              <tr>
                <th>Tên Permission</th>
                <th>Mô tả</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {permissions.map((perm) => (
                <tr key={perm.id}>
                  <td>{perm.name}</td>
                  <td>{perm.description}</td>
                  <td>
                    <button onClick={() => handleEditPermission(perm)}>Sửa</button>
                    <button onClick={() => handleDeletePermission(perm.id)}>Xóa</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <h4>{editingPermission ? "Sửa Permission" : "Thêm Permission mới"}</h4>
          <form className="permission-form" onSubmit={handleAddOrUpdatePermission}>
            <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
              <input 
                name="name" 
                placeholder="Tên permission (VD: READ_USERS, WRITE_PRODUCTS)" 
                value={permissionForm.name} 
                onChange={handlePermissionFormChange} 
                required 
              />
              <input 
                name="description" 
                placeholder="Mô tả permission" 
                value={permissionForm.description} 
                onChange={handlePermissionFormChange} 
              />
            </div>
            <div style={{ marginTop: '1rem' }}>
              <button type="submit">{editingPermission ? "Cập nhật Permission" : "Thêm Permission"}</button>
              {editingPermission && (
                <button 
                  type="button" 
                  onClick={() => { 
                    setEditingPermission(null); 
                    setPermissionForm({ name: "", description: "" }); 
                  }}
                >
                  Hủy
                </button>
              )}
            </div>
          </form>
        </div>
      )}
    </div>
  );
};

export default RolePermissionManager;