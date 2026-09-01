'use client';

import React, { useState } from 'react';
import { UserCheck, KeyRound, Mail, User, CheckCircle2, AlertCircle } from 'lucide-react';
import { Language, translations } from '@/lib/translations';
import { ApiClient } from '@/lib/api';

interface AuthCenterWidgetProps {
  lang: Language;
  onLoginSuccess: (username: string, pass: string) => void;
}

export const AuthCenterWidget: React.FC<AuthCenterWidgetProps> = ({ lang, onLoginSuccess }) => {
  const t = translations[lang];

  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [username, setUsername] = useState('user');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('password');
  const [msg, setMsg] = useState<{ text: string; type: 'success' | 'error' } | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setMsg(null);
    setLoading(true);

    try {
      if (mode === 'register') {
        const res = await ApiClient.registerUser({ username, email, password }, lang);
        setMsg({ text: res.message || t.msgRegisterSuccess, type: 'success' });
        setTimeout(() => setMode('login'), 1500);
      } else {
        onLoginSuccess(username, password);
        setMsg({ text: `${lang === 'tr' ? 'Giriş yapıldı' : 'Logged in'}: @${username}`, type: 'success' });
      }
    } catch (err: any) {
      setMsg({ text: err.message || t.msgError, type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card-clean p-6 sm:p-8 rounded-2xl max-w-md mx-auto space-y-5 bg-white border border-slate-200 shadow-sm">
      {/* Header */}
      <div className="text-center space-y-1">
        <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 border border-blue-100 mx-auto flex items-center justify-center mb-2">
          <UserCheck className="w-5 h-5" />
        </div>
        <h3 className="text-lg font-bold text-slate-900 tracking-tight">{t.profileTitle}</h3>
        <p className="text-xs text-slate-500">
          {lang === 'tr' ? 'Hesabınızı yönetin veya oturum açın' : 'Manage your account or sign in'}
        </p>
      </div>

      {/* Mode Switcher */}
      <div className="grid grid-cols-2 gap-1 p-1 bg-slate-100 rounded-xl text-xs font-semibold">
        <button
          onClick={() => setMode('login')}
          className={`py-2 rounded-lg transition-all cursor-pointer ${
            mode === 'login'
              ? 'bg-white text-slate-900 shadow-xs'
              : 'text-slate-500 hover:text-slate-900'
          }`}
        >
          {t.btnLogin}
        </button>
        <button
          onClick={() => setMode('register')}
          className={`py-2 rounded-lg transition-all cursor-pointer ${
            mode === 'register'
              ? 'bg-white text-slate-900 shadow-xs'
              : 'text-slate-500 hover:text-slate-900'
          }`}
        >
          {t.btnRegister}
        </button>
      </div>

      <form onSubmit={handleSubmit} className="space-y-3.5">
        {/* Username */}
        <div className="space-y-1">
          <label className="text-xs font-medium text-slate-700">{t.labelUsername}</label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
              <User className="w-4 h-4" />
            </div>
            <input
              type="text"
              required
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="user"
              className="w-full pl-9 pr-3.5 py-2.5 rounded-xl input-clean text-xs font-medium"
            />
          </div>
        </div>

        {/* Email (Register only) */}
        {mode === 'register' && (
          <div className="space-y-1 animate-fadeIn">
            <label className="text-xs font-medium text-slate-700">{t.labelEmail}</label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                <Mail className="w-4 h-4" />
              </div>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="user@example.com"
                className="w-full pl-9 pr-3.5 py-2.5 rounded-xl input-clean text-xs font-medium"
              />
            </div>
          </div>
        )}

        {/* Password */}
        <div className="space-y-1">
          <label className="text-xs font-medium text-slate-700">{t.labelPassword}</label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
              <KeyRound className="w-4 h-4" />
            </div>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              className="w-full pl-9 pr-3.5 py-2.5 rounded-xl input-clean text-xs font-medium"
            />
          </div>
        </div>

        {msg && (
          <div
            className={`p-2.5 rounded-lg border text-xs font-medium flex items-center gap-2 ${
              msg.type === 'success'
                ? 'bg-emerald-50 border-emerald-200 text-emerald-700'
                : 'bg-red-50 border-red-200 text-red-700'
            }`}
          >
            {msg.type === 'success' ? <CheckCircle2 className="w-4 h-4 shrink-0" /> : <AlertCircle className="w-4 h-4 shrink-0" />}
            <span>{msg.text}</span>
          </div>
        )}

        <button
          type="submit"
          disabled={loading}
          className="w-full py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs transition-colors cursor-pointer disabled:opacity-50 shadow-xs"
        >
          {loading ? (lang === 'tr' ? 'İşleniyor...' : 'Processing...') : mode === 'register' ? t.btnRegister : t.btnLogin}
        </button>
      </form>
    </div>
  );
};
