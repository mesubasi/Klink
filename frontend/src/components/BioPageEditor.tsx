'use client';

import React, { useState, useEffect } from 'react';
import {
  Sparkles,
  Link2,
  Plus,
  Trash2,
  ExternalLink,
  Eye,
  Check,
  Save,
  Palette,
  Share2,
  Globe,
  Flame,
  Mail,
  ArrowUp,
  ArrowDown,
  QrCode,
  Smartphone,
  Activity,
  Layers
} from 'lucide-react';
import { Language, translations } from '@/lib/translations';
import { BioPageDto, BioLinkItemDto, BioPageUpdateRequest } from '@/lib/types';
import { ApiClient } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';

// Helper Social SVGs
const TwitterIcon = ({ className = "w-4 h-4" }: { className?: string }) => (
  <svg className={`${className} fill-current`} viewBox="0 0 24 24">
    <path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z"/>
  </svg>
);

const GithubIcon = ({ className = "w-4 h-4" }: { className?: string }) => (
  <svg className={`${className} fill-current`} viewBox="0 0 24 24">
    <path d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.53 1.032 1.53 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"/>
  </svg>
);

const InstagramIcon = ({ className = "w-4 h-4" }: { className?: string }) => (
  <svg className={`${className} fill-none stroke-current stroke-2`} viewBox="0 0 24 24">
    <rect x="2" y="2" width="20" height="20" rx="5" ry="5"/>
    <path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"/>
    <line x1="17.5" y1="6.5" x2="17.51" y2="6.5"/>
  </svg>
);

const YoutubeIcon = ({ className = "w-4 h-4" }: { className?: string }) => (
  <svg className={`${className} fill-current`} viewBox="0 0 24 24">
    <path d="M23.498 6.186a3.016 3.016 0 0 0-2.122-2.136C19.505 3.545 12 3.545 12 3.545s-7.505 0-9.377.505A3.017 3.017 0 0 0 .502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a3.016 3.016 0 0 0 2.122 2.136c1.871.505 9.376.505 9.376.505s7.505 0 9.377-.505a3.015 3.015 0 0 0 2.122-2.136C24 15.93 24 12 24 12s0-3.93-.502-5.814zM9.545 15.568V8.432L15.818 12l-6.273 3.568z"/>
  </svg>
);

const LinkedinIcon = ({ className = "w-4 h-4" }: { className?: string }) => (
  <svg className={`${className} fill-current`} viewBox="0 0 24 24">
    <path d="M19 3a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h14m-.5 15.5v-5.3a3.26 3.26 0 0 0-3.26-3.26c-.85 0-1.84.52-2.28 1.3v-1.11h-2.79v8.37h2.79v-4.93c0-.77.62-1.4 1.39-1.4a1.4 1.4 0 0 1 1.4 1.4v4.93h2.75M6.88 8.56a1.68 1.68 0 0 0 1.68-1.68c0-.93-.75-1.69-1.68-1.69a1.69 1.69 0 0 0-1.69 1.69c0 .93.76 1.68 1.69 1.68m1.39 9.94v-8.37H5.5v8.37h2.77z"/>
  </svg>
);

interface BioPageEditorProps {
  lang: Language;
  authUser?: { u?: string; p?: string; token?: string } | null;
}

const THEMES = [
  { id: 'classic_dark', name: 'Midnight Onyx', color: 'bg-zinc-950 text-white border-zinc-800' },
  { id: 'clean_white', name: 'Crisp Minimal', color: 'bg-white text-zinc-950 border-zinc-200 shadow-xs' },
  { id: 'sunset_gradient', name: 'Sunset Glow', color: 'bg-gradient-to-r from-purple-900 to-rose-800 text-white' },
  { id: 'cyberpunk', name: 'Cyberpunk Neon', color: 'bg-[#0a0a14] text-cyan-400 border-cyan-500/50' },
  { id: 'emerald_forest', name: 'Emerald Forest', color: 'bg-emerald-950 text-emerald-300 border-emerald-800' },
];

export const BioPageEditor: React.FC<BioPageEditorProps> = ({ lang, authUser }) => {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [savedSuccess, setSavedSuccess] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  // Form states
  const [username, setUsername] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [bioDescription, setBioDescription] = useState('');
  const [avatarUrl, setAvatarUrl] = useState('');
  const [theme, setTheme] = useState('classic_dark');
  const [viewCount, setViewCount] = useState(0);

  // Social handles
  const [twitter, setTwitter] = useState('');
  const [github, setGithub] = useState('');
  const [instagram, setInstagram] = useState('');
  const [youtube, setYoutube] = useState('');
  const [linkedin, setLinkedin] = useState('');
  const [email, setEmail] = useState('');

  // Links list
  const [links, setLinks] = useState<BioLinkItemDto[]>([]);

  // New link draft
  const [newTitle, setNewTitle] = useState('');
  const [newUrl, setNewUrl] = useState('');
  const [newHighlighted, setNewHighlighted] = useState(false);

  useEffect(() => {
    loadBioData();
  }, [authUser]);

  const loadBioData = async () => {
    setLoading(true);
    try {
      const data = await ApiClient.getMyBioPage(lang, authUser || undefined);
      setUsername(data.username || authUser?.u || 'user');
      setDisplayName(data.displayName || authUser?.u || 'User');
      setBioDescription(data.bioDescription || '');
      setAvatarUrl(data.avatarUrl || '');
      setTheme(data.theme || 'classic_dark');
      setViewCount(data.viewCount || 0);
      setLinks(data.links || []);

      if (data.socialLinks) {
        try {
          const parsed = JSON.parse(data.socialLinks);
          setTwitter(parsed.twitter || '');
          setGithub(parsed.github || '');
          setInstagram(parsed.instagram || '');
          setYoutube(parsed.youtube || '');
          setLinkedin(parsed.linkedin || '');
          setEmail(parsed.email || '');
        } catch {}
      }
    } catch (e: any) {
      console.warn('Bio load warning:', e);
    } finally {
      setLoading(false);
    }
  };

  const handleAddLink = () => {
    if (!newTitle.trim() || !newUrl.trim()) return;

    const newLink: BioLinkItemDto = {
      id: 'temp-' + Date.now(),
      title: newTitle.trim(),
      url: newUrl.trim(),
      highlighted: newHighlighted,
      active: true,
      sortOrder: links.length,
      clickCount: 0,
    };

    setLinks([...links, newLink]);
    setNewTitle('');
    setNewUrl('');
    setNewHighlighted(false);
  };

  const handleRemoveLink = (index: number) => {
    setLinks(links.filter((_, i) => i !== index));
  };

  const handleToggleHighlight = (index: number) => {
    const updated = [...links];
    updated[index].highlighted = !updated[index].highlighted;
    setLinks(updated);
  };

  const handleMoveLink = (index: number, direction: 'up' | 'down') => {
    if (direction === 'up' && index === 0) return;
    if (direction === 'down' && index === links.length - 1) return;

    const targetIdx = direction === 'up' ? index - 1 : index + 1;
    const updated = [...links];
    const temp = updated[index];
    updated[index] = updated[targetIdx];
    updated[targetIdx] = temp;
    setLinks(updated);
  };

  const handleSave = async () => {
    setSaving(true);
    setErrorMsg('');
    setSavedSuccess(false);

    const socialLinksObj = {
      twitter: twitter.trim(),
      github: github.trim(),
      instagram: instagram.trim(),
      youtube: youtube.trim(),
      linkedin: linkedin.trim(),
      email: email.trim(),
    };

    const req: BioPageUpdateRequest = {
      username: username.trim(),
      displayName: displayName.trim(),
      bioDescription: bioDescription.trim(),
      avatarUrl: avatarUrl.trim(),
      theme: theme,
      socialLinks: JSON.stringify(socialLinksObj),
      links: links,
    };

    try {
      const res = await ApiClient.updateMyBioPage(req, lang, authUser || undefined);
      setSavedSuccess(true);
      setTimeout(() => setSavedSuccess(false), 3000);
    } catch (e: any) {
      setErrorMsg(e.message || 'Bio sayfası kaydedilirken bir hata oluştu.');
    } finally {
      setSaving(false);
    }
  };

  const publicBioUrl = typeof window !== 'undefined' ? `${window.location.origin}/bio/${username}` : `/bio/${username}`;
  const totalLinkClicks = links.reduce((acc, l) => acc + (l.clickCount || 0), 0);

  if (loading) {
    return (
      <div className="p-12 text-center text-zinc-500">
        <div className="w-8 h-8 rounded-full border-2 border-zinc-950 border-t-transparent animate-spin mx-auto mb-3" />
        <p className="text-xs font-semibold">Bio ayarları yükleniyor...</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Top Banner Stats & Action */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 p-5 rounded-2xl bg-gradient-to-r from-purple-950/10 via-zinc-900/5 to-purple-900/10 border border-purple-200/40">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-purple-600 text-white flex items-center justify-center shadow-md">
            <Sparkles className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="font-extrabold text-sm text-zinc-950">Link-in-Bio Yönetim Paneli</h3>
              <Badge variant="secondary" className="bg-purple-100 text-purple-800 text-[10px]">
                Mikro Profil Sayfası
              </Badge>
            </div>
            <p className="text-xs text-zinc-500 mt-0.5">
              Kişisel mikro iniş sayfanızı tasarlayın, tüm linklerinizi tek bir profilde toplayın.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2 w-full sm:w-auto">
          <Button
            variant="outline"
            asChild
            className="text-xs h-9 border-zinc-200 gap-1.5 flex-1 sm:flex-initial"
          >
            <a href={`/bio/${username}`} target="_blank" rel="noreferrer">
              <Eye className="w-3.5 h-3.5 text-zinc-600" />
              <span>Bio Sayfamı Gör</span>
              <ExternalLink className="w-3 h-3 text-zinc-400" />
            </a>
          </Button>

          <Button
            onClick={handleSave}
            disabled={saving}
            className="text-xs h-9 bg-purple-600 hover:bg-purple-700 text-white gap-1.5 font-bold flex-1 sm:flex-initial"
          >
            {saving ? (
              <span className="animate-spin">⏳</span>
            ) : savedSuccess ? (
              <>
                <Check className="w-3.5 h-3.5 text-white" />
                <span>Kaydedildi!</span>
              </>
            ) : (
              <>
                <Save className="w-3.5 h-3.5" />
                <span>Değişiklikleri Kaydet</span>
              </>
            )}
          </Button>
        </div>
      </div>

      {errorMsg && (
        <div className="p-3 rounded-xl bg-red-50 border border-red-200 text-red-700 text-xs font-semibold">
          ⚠️ {errorMsg}
        </div>
      )}

      {/* Main Grid: Left Controls, Right Live Mockup */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Column: Form Controls (7 cols) */}
        <div className="lg:col-span-7 space-y-6">
          {/* Telemetry Metrics */}
          <div className="grid grid-cols-2 gap-3">
            <Card className="p-4 border-zinc-200/80 bg-white flex items-center justify-between">
              <div>
                <span className="text-[11px] font-semibold text-zinc-400 block">Toplam Bio Görüntülenme</span>
                <span className="text-xl font-extrabold text-zinc-950">{viewCount.toLocaleString()}</span>
              </div>
              <div className="w-8 h-8 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center">
                <Eye className="w-4 h-4" />
              </div>
            </Card>

            <Card className="p-4 border-zinc-200/80 bg-white flex items-center justify-between">
              <div>
                <span className="text-[11px] font-semibold text-zinc-400 block">Toplam Link Tıklaması</span>
                <span className="text-xl font-extrabold text-zinc-950">{totalLinkClicks.toLocaleString()}</span>
              </div>
              <div className="w-8 h-8 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
                <Activity className="w-4 h-4" />
              </div>
            </Card>
          </div>

          {/* Section 1: Profil Bilgileri */}
          <Card className="p-5 border-zinc-200/80 bg-white space-y-4">
            <h4 className="text-xs font-bold text-zinc-950 uppercase tracking-wider flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-purple-600" />
              <span>1. Profil ve Kimlik Bilgileri</span>
            </h4>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div className="space-y-1">
                <label className="text-xs font-semibold text-zinc-700">Kullanıcı Adı (URL slug)</label>
                <div className="relative">
                  <span className="absolute left-3 top-2.5 text-xs text-zinc-400 font-mono">klink.to/bio/</span>
                  <Input
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    placeholder="kullaniciadi"
                    className="text-xs pl-24 font-mono font-bold"
                  />
                </div>
              </div>

              <div className="space-y-1">
                <label className="text-xs font-semibold text-zinc-700">Görünen Ad / Başlık</label>
                <Input
                  value={displayName}
                  onChange={(e) => setDisplayName(e.target.value)}
                  placeholder="Emin & Tech Co."
                  className="text-xs font-semibold"
                />
              </div>
            </div>

            <div className="space-y-1">
              <label className="text-xs font-semibold text-zinc-700">Profil Fotoğrafı (Avatar URL)</label>
              <Input
                value={avatarUrl}
                onChange={(e) => setAvatarUrl(e.target.value)}
                placeholder="https://images.unsplash.com/... veya resim linki"
                className="text-xs font-mono"
              />
            </div>

            <div className="space-y-1">
              <label className="text-xs font-semibold text-zinc-700">Kısa Biyografi (Hakkında)</label>
              <textarea
                value={bioDescription}
                onChange={(e) => setBioDescription(e.target.value)}
                rows={2}
                placeholder="Tüm projelerime, sosyal kanallarıma ve içeriklerime aşağıdan ulaşabilirsiniz..."
                className="w-full text-xs p-2.5 rounded-xl border border-zinc-200 bg-white focus:outline-none focus:ring-2 focus:ring-purple-600"
              />
            </div>
          </Card>

          {/* Section 2: Tema Seçimi */}
          <Card className="p-5 border-zinc-200/80 bg-white space-y-3">
            <h4 className="text-xs font-bold text-zinc-950 uppercase tracking-wider flex items-center gap-2">
              <Palette className="w-4 h-4 text-purple-600" />
              <span>2. Görsel Tema & Stil</span>
            </h4>

            <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5">
              {THEMES.map((t) => (
                <button
                  key={t.id}
                  type="button"
                  onClick={() => setTheme(t.id)}
                  className={`p-3 rounded-xl border text-left transition-all cursor-pointer flex flex-col justify-between h-16 ${
                    theme === t.id
                      ? 'border-purple-600 ring-2 ring-purple-600/20 shadow-xs'
                      : 'border-zinc-200 hover:border-zinc-300'
                  }`}
                >
                  <span className="text-[11px] font-bold text-zinc-900 block truncate">{t.name}</span>
                  <div className={`w-full h-3.5 rounded-md ${t.color}`} />
                </button>
              ))}
            </div>
          </Card>

          {/* Section 3: Sosyal Medya İkonları */}
          <Card className="p-5 border-zinc-200/80 bg-white space-y-3">
            <h4 className="text-xs font-bold text-zinc-950 uppercase tracking-wider flex items-center gap-2">
              <Share2 className="w-4 h-4 text-purple-600" />
              <span>3. Sosyal Medya Hesapları (İkon Barı)</span>
            </h4>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5 text-xs">
              <div className="flex items-center gap-2">
                <TwitterIcon className="w-4 h-4 text-zinc-500 shrink-0" />
                <Input
                  value={twitter}
                  onChange={(e) => setTwitter(e.target.value)}
                  placeholder="Twitter / X Linki veya @handle"
                  className="text-xs h-8"
                />
              </div>

              <div className="flex items-center gap-2">
                <InstagramIcon className="w-4 h-4 text-pink-600 shrink-0" />
                <Input
                  value={instagram}
                  onChange={(e) => setInstagram(e.target.value)}
                  placeholder="Instagram Linki"
                  className="text-xs h-8"
                />
              </div>

              <div className="flex items-center gap-2">
                <GithubIcon className="w-4 h-4 text-zinc-900 shrink-0" />
                <Input
                  value={github}
                  onChange={(e) => setGithub(e.target.value)}
                  placeholder="GitHub Profil Linki"
                  className="text-xs h-8"
                />
              </div>

              <div className="flex items-center gap-2">
                <YoutubeIcon className="w-4 h-4 text-red-600 shrink-0" />
                <Input
                  value={youtube}
                  onChange={(e) => setYoutube(e.target.value)}
                  placeholder="YouTube Kanal Linki"
                  className="text-xs h-8"
                />
              </div>

              <div className="flex items-center gap-2">
                <LinkedinIcon className="w-4 h-4 text-blue-600 shrink-0" />
                <Input
                  value={linkedin}
                  onChange={(e) => setLinkedin(e.target.value)}
                  placeholder="LinkedIn Linki"
                  className="text-xs h-8"
                />
              </div>

              <div className="flex items-center gap-2">
                <Mail className="w-4 h-4 text-emerald-600 shrink-0" />
                <Input
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="İletişim E-posta Adresi"
                  className="text-xs h-8"
                />
              </div>
            </div>
          </Card>

          {/* Section 4: Bağlantılar Listesi */}
          <Card className="p-5 border-zinc-200/80 bg-white space-y-4">
            <div className="flex items-center justify-between">
              <h4 className="text-xs font-bold text-zinc-950 uppercase tracking-wider flex items-center gap-2">
                <Layers className="w-4 h-4 text-purple-600" />
                <span>4. Bio Bağlantıları ({links.length})</span>
              </h4>
            </div>

            {/* Existing Links List */}
            <div className="space-y-2.5">
              {links.map((link, idx) => (
                <div
                  key={link.id || idx}
                  className={`p-3 rounded-xl border transition-all flex items-center justify-between gap-3 ${
                    link.highlighted ? 'border-amber-300 bg-amber-50/40' : 'border-zinc-200 bg-zinc-50/60'
                  }`}
                >
                  <div className="flex items-center gap-2.5 min-w-0">
                    <div className="flex flex-col gap-0.5">
                      <button
                        type="button"
                        onClick={() => handleMoveLink(idx, 'up')}
                        disabled={idx === 0}
                        className="text-zinc-400 hover:text-zinc-700 disabled:opacity-20 cursor-pointer"
                      >
                        <ArrowUp className="w-3 h-3" />
                      </button>
                      <button
                        type="button"
                        onClick={() => handleMoveLink(idx, 'down')}
                        disabled={idx === links.length - 1}
                        className="text-zinc-400 hover:text-zinc-700 disabled:opacity-20 cursor-pointer"
                      >
                        <ArrowDown className="w-3 h-3" />
                      </button>
                    </div>

                    <div className="min-w-0">
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-xs text-zinc-900 truncate">{link.title}</span>
                        {link.highlighted && (
                          <Badge variant="warning" className="text-[9px] py-0 px-1.5 gap-0.5">
                            <Flame className="w-2.5 h-2.5" />
                            Öne Çıkan
                          </Badge>
                        )}
                      </div>
                      <span className="text-[11px] font-mono text-zinc-500 block truncate">{link.url}</span>
                    </div>
                  </div>

                  <div className="flex items-center gap-1.5 shrink-0">
                    <Badge variant="secondary" className="font-mono text-[10px] text-zinc-600">
                      {link.clickCount || 0} tık
                    </Badge>

                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      onClick={() => handleToggleHighlight(idx)}
                      className={`h-7 px-2 text-[11px] ${link.highlighted ? 'text-amber-600' : 'text-zinc-500'}`}
                      title="Öne Çıkar / Parlat"
                    >
                      <Flame className="w-3.5 h-3.5" />
                    </Button>

                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      onClick={() => handleRemoveLink(idx)}
                      className="h-7 w-7 p-0 text-red-500 hover:bg-red-50 hover:text-red-700"
                      title="Sil"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </Button>
                  </div>
                </div>
              ))}
            </div>

            {/* Add New Link Box */}
            <div className="p-3.5 rounded-xl border border-dashed border-purple-300 bg-purple-50/30 space-y-2.5">
              <span className="text-xs font-bold text-purple-950 flex items-center gap-1.5">
                <Plus className="w-3.5 h-3.5 text-purple-600" />
                <span>Yeni Bağlantı Ekle</span>
              </span>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                <Input
                  value={newTitle}
                  onChange={(e) => setNewTitle(e.target.value)}
                  placeholder="Başlık (Örn: YouTube Kanalım)"
                  className="text-xs h-8 bg-white"
                />
                <Input
                  value={newUrl}
                  onChange={(e) => setNewUrl(e.target.value)}
                  placeholder="Hedef URL (https://...)"
                  className="text-xs h-8 bg-white font-mono"
                />
              </div>

              <div className="flex items-center justify-between pt-1">
                <label className="flex items-center gap-1.5 text-xs text-zinc-700 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={newHighlighted}
                    onChange={(e) => setNewHighlighted(e.target.checked)}
                    className="rounded text-purple-600 focus:ring-purple-500"
                  />
                  <span>Öne Çıkar (Animasyonlu & Parlayan Buton)</span>
                </label>

                <Button
                  type="button"
                  onClick={handleAddLink}
                  disabled={!newTitle.trim() || !newUrl.trim()}
                  className="text-xs h-8 bg-purple-600 hover:bg-purple-700 text-white font-bold"
                >
                  <Plus className="w-3 h-3 mr-1" />
                  <span>Listeye Ekle</span>
                </Button>
              </div>
            </div>
          </Card>
        </div>

        {/* Right Column: Interactive Phone Mockup Preview (5 cols) */}
        <div className="lg:col-span-5 flex flex-col items-center">
          <div className="sticky top-6 w-full max-w-[340px] space-y-3">
            <div className="flex items-center justify-between text-xs font-bold text-zinc-700 px-1">
              <span className="flex items-center gap-1.5">
                <Smartphone className="w-4 h-4 text-purple-600" />
                <span>Canlı Önizleme (Mobile Mockup)</span>
              </span>
              <Badge variant="secondary" className="text-[10px]">
                {THEMES.find((t) => t.id === theme)?.name}
              </Badge>
            </div>

            {/* Phone Frame */}
            <div className="w-full h-[580px] rounded-[36px] bg-zinc-950 p-3 shadow-2xl border-4 border-zinc-800 relative overflow-hidden flex flex-col">
              {/* Notch */}
              <div className="w-24 h-4 bg-zinc-900 rounded-full mx-auto mb-2 shrink-0 flex items-center justify-center">
                <div className="w-2.5 h-2.5 rounded-full bg-zinc-800" />
              </div>

              {/* Screen Content */}
              <div
                className={`flex-1 rounded-[24px] p-4 overflow-y-auto custom-scrollbar flex flex-col items-center justify-between text-center ${
                  theme === 'clean_white'
                    ? 'bg-slate-50 text-zinc-900'
                    : theme === 'sunset_gradient'
                    ? 'bg-gradient-to-br from-indigo-950 via-purple-900 to-rose-900 text-white'
                    : theme === 'cyberpunk'
                    ? 'bg-[#0a0a14] text-cyan-50'
                    : theme === 'emerald_forest'
                    ? 'bg-gradient-to-b from-emerald-950 via-zinc-950 to-zinc-950 text-white'
                    : 'bg-zinc-950 text-white'
                }`}
              >
                {/* Header */}
                <div className="w-full flex flex-col items-center space-y-2 pt-2">
                  <div className="relative">
                    {avatarUrl ? (
                      <img
                        src={avatarUrl}
                        alt="Avatar"
                        className="w-16 h-16 rounded-full object-cover shadow-lg ring-2 ring-white/20"
                      />
                    ) : (
                      <div className="w-16 h-16 rounded-full bg-gradient-to-tr from-purple-600 to-indigo-500 flex items-center justify-center text-white text-xl font-extrabold shadow-lg">
                        {(displayName || username || 'U').charAt(0).toUpperCase()}
                      </div>
                    )}
                  </div>

                  <div className="space-y-0.5">
                    <h5 className="font-extrabold text-sm tracking-tight">{displayName || 'Görünen Ad'}</h5>
                    <p className="text-[10px] opacity-60 font-mono">@{username || 'kullaniciadi'}</p>
                  </div>

                  {bioDescription && (
                    <p className="text-[10px] opacity-75 max-w-[220px] leading-snug">
                      {bioDescription}
                    </p>
                  )}

                  {/* Mock Socials */}
                  {(twitter || github || instagram || youtube || linkedin || email) && (
                    <div className="flex items-center justify-center gap-1.5 pt-1">
                      {twitter && <div className="w-6 h-6 rounded-lg bg-white/10 flex items-center justify-center text-[10px]"><TwitterIcon className="w-3 h-3" /></div>}
                      {github && <div className="w-6 h-6 rounded-lg bg-white/10 flex items-center justify-center text-[10px]"><GithubIcon className="w-3 h-3" /></div>}
                      {instagram && <div className="w-6 h-6 rounded-lg bg-white/10 flex items-center justify-center text-[10px]"><InstagramIcon className="w-3 h-3" /></div>}
                      {youtube && <div className="w-6 h-6 rounded-lg bg-white/10 flex items-center justify-center text-[10px]"><YoutubeIcon className="w-3 h-3" /></div>}
                      {linkedin && <div className="w-6 h-6 rounded-lg bg-white/10 flex items-center justify-center text-[10px]"><LinkedinIcon className="w-3 h-3" /></div>}
                      {email && <div className="w-6 h-6 rounded-lg bg-white/10 flex items-center justify-center text-[10px]"><Mail className="w-3 h-3" /></div>}
                    </div>
                  )}
                </div>

                {/* Mock Links List */}
                <div className="w-full space-y-2 py-3">
                  {links.length > 0 ? (
                    links.map((link, idx) => (
                      <div
                        key={idx}
                        className={`w-full p-2.5 rounded-xl border text-left text-xs font-semibold flex items-center justify-between transition-all ${
                          theme === 'clean_white'
                            ? 'bg-white border-zinc-200 shadow-xs'
                            : 'bg-white/10 border-white/15'
                        } ${link.highlighted ? 'ring-2 ring-amber-400/60 shadow-md' : ''}`}
                      >
                        <div className="flex items-center gap-2 min-w-0">
                          {link.highlighted ? (
                            <Flame className="w-3.5 h-3.5 text-amber-400 shrink-0" />
                          ) : (
                            <Globe className="w-3.5 h-3.5 opacity-60 shrink-0" />
                          )}
                          <span className="truncate text-[11px]">{link.title}</span>
                        </div>
                        <ExternalLink className="w-3 h-3 opacity-40 shrink-0" />
                      </div>
                    ))
                  ) : (
                    <div className="p-4 rounded-xl bg-white/5 text-[11px] opacity-50">
                      Henüz link eklenmedi
                    </div>
                  )}
                </div>

                {/* Mock Footer */}
                <div className="pt-2 pb-1 opacity-50 text-[9px]">
                  <span>⚡ Powered by Klink</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
