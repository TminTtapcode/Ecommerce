import React, { useCallback, useEffect, useRef, useState } from 'react';
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
  const lifecycle = useRef(0);
  const invalidateRequests = useCallback(() => { ++lifecycle.current; }, []);
  const apply = useCallback((value: Profile) => {
    setProfile(value); setFullName(value.fullName); setPhone(value.phone ?? '');
  }, []);
  const load = useCallback(async () => {
    const version = lifecycle.current;
    setLoading(true); setError('');
    try { const value = await loadProfile(); if (version === lifecycle.current && value) apply(value); }
    catch (err) { if (version === lifecycle.current) setError(getApiError(err, 'Không tải được hồ sơ. Vui lòng thử lại.').message); }
    finally { if (version === lifecycle.current) setLoading(false); }
  }, [loadProfile, apply]);
  useEffect(() => {
    ++lifecycle.current; setProfile(null); setNotice(''); setSaving(false);
    void load();
    return invalidateRequests;
  }, [load, user?.userId, invalidateRequests]);
  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!profile || saving) return;
    const version = lifecycle.current;
    setSaving(true); setError(''); setNotice('');
    try {
      const value = await saveProfile({ fullName: fullName.trim(), phone: phone.trim() || null });
      if (version === lifecycle.current && value) { apply(value); setNotice('Đã cập nhật hồ sơ.'); }
    } catch (err) { if (version === lifecycle.current) setError(getApiError(err, 'Không lưu được hồ sơ.').message); }
    finally { if (version === lifecycle.current) setSaving(false); }
  };
  if (!isAuthenticated || !user) return <Navigate to="/login" replace />;
  return <div className="max-w-3xl mx-auto py-8 px-4"><div className="bg-white rounded-xl shadow p-6">
    <h1 className="text-2xl font-bold mb-6">Hồ sơ của tôi</h1>
    {error && <p role="alert" className="text-red-600 mb-4">{error}</p>}
    {notice && <p role="status" className="text-green-700 mb-4">{notice}</p>}
    {loading ? <p>Đang tải hồ sơ...</p> : !profile ? <button onClick={load} className="border rounded px-4 py-2">Thử lại</button> : <>
      <div className="flex items-center gap-4 mb-6">
        <div className="w-16 h-16 rounded-full bg-orange-100 text-orange-600 flex items-center justify-center text-2xl">{Array.from(profile.fullName)[0]?.toUpperCase()}</div>
        <div><p className="font-semibold">{profile.fullName}</p><p className="text-gray-500">{profile.email}</p></div>
      </div>
      <form onSubmit={submit}><fieldset disabled={saving} className="space-y-4">
        <label className="block">Email<input readOnly value={profile.email} className="block w-full border rounded p-2 bg-gray-50" /></label>
        <label className="block">Họ và tên<input name="fullName" required maxLength={100} value={fullName} onChange={e => setFullName(e.target.value)} className="block w-full border rounded p-2" autoComplete="name" /></label>
        <label className="block">Số điện thoại (tùy chọn)<input name="phone" type="tel" maxLength={20} value={phone} onChange={e => setPhone(e.target.value)} className="block w-full border rounded p-2" autoComplete="tel" /></label>
        <button disabled={!fullName.trim()} className="bg-orange-500 text-white px-5 py-2 rounded disabled:opacity-50">{saving ? 'Đang lưu...' : 'Lưu hồ sơ'}</button>
      </fieldset></form>
    </>}
  </div></div>;
};
