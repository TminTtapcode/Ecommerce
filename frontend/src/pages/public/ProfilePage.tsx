import React, { useEffect, useState } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { Navigate } from 'react-router-dom';
import type { Profile } from '../../api/userApi';
import { getApiError } from '../../api/apiError';

export const ProfilePage: React.FC = () => {
  const { user, isAuthenticated, loadProfile, saveProfile } = useAuth();
  const [profile, setProfile] = useState<Profile | null>(null);
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  const fetchProfile = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await loadProfile();
      if (data) {
        setProfile(data);
        setFullName(data.fullName);
        setPhone(data.phone ?? '');
      }
    } catch (err) {
      setError(getApiError(err, 'Không tải được hồ sơ. Vui lòng thử lại.').message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isAuthenticated) {
      fetchProfile();
    }
  }, [isAuthenticated]);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!profile || saving) return;

    setSaving(true);
    setError('');
    setNotice('');
    try {
      const updated = await saveProfile({
        fullName: fullName.trim(),
        phone: phone.trim() || null,
      });
      if (updated) {
        setProfile(updated);
        setFullName(updated.fullName);
        setPhone(updated.phone ?? '');
        setNotice('Đã cập nhật hồ sơ thành công.');
      }
    } catch (err) {
      setError(getApiError(err, 'Không lưu được hồ sơ.').message);
    } finally {
      setSaving(false);
    }
  };

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="max-w-3xl mx-auto py-8 px-4">
      <div className="bg-white rounded-xl shadow p-6">
        <h1 className="text-2xl font-bold mb-6">Hồ sơ của tôi</h1>

        {error && (
          <p role="alert" className="text-red-600 mb-4 p-3 bg-red-50 rounded-lg">
            {error}
          </p>
        )}
        {notice && (
          <p role="status" className="text-green-700 mb-4 p-3 bg-green-50 rounded-lg">
            {notice}
          </p>
        )}

        {loading ? (
          <p className="text-gray-500 py-4">Đang tải hồ sơ...</p>
        ) : !profile ? (
          <button
            onClick={fetchProfile}
            className="border border-orange-500 text-orange-500 rounded px-4 py-2 hover:bg-orange-50"
          >
            Thử lại
          </button>
        ) : (
          <>
            <div className="flex items-center gap-4 mb-6">
              <div className="w-16 h-16 rounded-full bg-orange-100 text-orange-600 flex items-center justify-center text-2xl font-bold">
                {profile.fullName.charAt(0).toUpperCase()}
              </div>
              <div>
                <p className="font-semibold text-lg">{profile.fullName}</p>
                <p className="text-gray-500">{profile.email}</p>
              </div>
            </div>

            <form onSubmit={handleSubmit}>
              <fieldset disabled={saving} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                  <input
                    type="email"
                    readOnly
                    value={profile.email}
                    className="block w-full border rounded-lg p-2.5 bg-gray-50 text-gray-500 cursor-not-allowed"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Họ và tên</label>
                  <input
                    type="text"
                    name="fullName"
                    required
                    maxLength={100}
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    className="block w-full border rounded-lg p-2.5 focus:ring-2 focus:ring-orange-500 focus:outline-none"
                    autoComplete="name"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Số điện thoại (tùy chọn)
                  </label>
                  <input
                    type="tel"
                    name="phone"
                    maxLength={20}
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)}
                    className="block w-full border rounded-lg p-2.5 focus:ring-2 focus:ring-orange-500 focus:outline-none"
                    autoComplete="tel"
                  />
                </div>

                <div className="pt-2">
                  <button
                    type="submit"
                    disabled={!fullName.trim() || saving}
                    className="bg-orange-500 hover:bg-orange-600 text-white font-medium px-6 py-2.5 rounded-lg disabled:opacity-50 transition"
                  >
                    {saving ? 'Đang lưu...' : 'Lưu hồ sơ'}
                  </button>
                </div>
              </fieldset>
            </form>
          </>
        )}
      </div>
    </div>
  );
};
