'use client';

import React, { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import {
  Check,
  Copy,
  Share2,
  ExternalLink,
  QrCode,
  ShieldCheck,
  Globe,
  Sparkles,
  Mail,
  Flame,
  ArrowRight,
  Eye,
  Link2,
  X
} from 'lucide-react';
import { ApiClient } from '@/lib/api';
import { BioPageDto, BioLinkItemDto } from '@/lib/types';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';

// Social Icon helper
const getSocialIcon = (network: string) => {
  switch (network.toLowerCase()) {
    case 'twitter':
    case 'x':
      return (
        <svg className="w-4 h-4 fill-current" viewBox="0 0 24 24">
          <path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z"/>
        </svg>
      );
    case 'github':
      return (
        <svg className="w-4 h-4 fill-current" viewBox="0 0 24 24">
          <path d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.53 1.032 1.53 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"/>
        </svg>
      );
    case 'instagram':
      return (
        <svg className="w-4 h-4 fill-none stroke-current stroke-2" viewBox="0 0 24 24">
          <rect x="2" y="2" width="20" height="20" rx="5" ry="5"/>
          <path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"/>
          <line x1="17.5" y1="6.5" x2="17.51" y2="6.5"/>
        </svg>
      );
    case 'youtube':
      return (
        <svg className="w-4 h-4 fill-current" viewBox="0 0 24 24">
          <path d="M23.498 6.186a3.016 3.016 0 0 0-2.122-2.136C19.505 3.545 12 3.545 12 3.545s-7.505 0-9.377.505A3.017 3.017 0 0 0 .502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a3.016 3.016 0 0 0 2.122 2.136c1.871.505 9.376.505 9.376.505s7.505 0 9.377-.505a3.015 3.015 0 0 0 2.122-2.136C24 15.93 24 12 24 12s0-3.93-.502-5.814zM9.545 15.568V8.432L15.818 12l-6.273 3.568z"/>
        </svg>
      );
    case 'linkedin':
      return (
        <svg className="w-4 h-4 fill-current" viewBox="0 0 24 24">
          <path d="M19 3a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h14m-.5 15.5v-5.3a3.26 3.26 0 0 0-3.26-3.26c-.85 0-1.84.52-2.28 1.3v-1.11h-2.79v8.37h2.79v-4.93c0-.77.62-1.4 1.39-1.4a1.4 1.4 0 0 1 1.4 1.4v4.93h2.75M6.88 8.56a1.68 1.68 0 0 0 1.68-1.68c0-.93-.75-1.69-1.68-1.69a1.69 1.69 0 0 0-1.69 1.69c0 .93.76 1.68 1.69 1.68m1.39 9.94v-8.37H5.5v8.37h2.77z"/>
        </svg>
      );
    case 'email':
    case 'mail':
      return <Mail className="w-4 h-4" />;
    default:
      return <Globe className="w-4 h-4" />;
  }
};

// Theme configurations
const THEME_STYLES: Record<string, {
  bg: string;
  cardBg: string;
  cardBorder: string;
  textColor: string;
  subtextColor: string;
  linkHover: string;
  highlightBorder: string;
  highlightGlow: string;
}> = {
  classic_dark: {
    bg: 'bg-zinc-950 text-white',
    cardBg: 'bg-zinc-900/90 hover:bg-zinc-800/90',
    cardBorder: 'border-zinc-800/80 hover:border-zinc-600',
    textColor: 'text-white',
    subtextColor: 'text-zinc-400',
    linkHover: 'hover:scale-[1.015]',
    highlightBorder: 'border-white/40 ring-2 ring-white/20',
    highlightGlow: 'shadow-[0_0_20px_rgba(255,255,255,0.15)]',
  },
  clean_white: {
    bg: 'bg-slate-50 text-zinc-900',
    cardBg: 'bg-white hover:bg-slate-50',
    cardBorder: 'border-zinc-200/90 hover:border-zinc-400 shadow-sm',
    textColor: 'text-zinc-900',
    subtextColor: 'text-zinc-500',
    linkHover: 'hover:scale-[1.015]',
    highlightBorder: 'border-zinc-900 ring-2 ring-zinc-900/10',
    highlightGlow: 'shadow-md',
  },
  sunset_gradient: {
    bg: 'bg-gradient-to-br from-slate-950 via-purple-950 to-rose-950 text-white',
    cardBg: 'bg-white/10 hover:bg-white/15 backdrop-blur-md',
    cardBorder: 'border-white/15 hover:border-rose-300/40',
    textColor: 'text-white',
    subtextColor: 'text-rose-200/70',
    linkHover: 'hover:scale-[1.015]',
    highlightBorder: 'border-rose-400/60 ring-2 ring-rose-400/30',
    highlightGlow: 'shadow-[0_0_25px_rgba(244,63,94,0.25)]',
  },
  cyberpunk: {
    bg: 'bg-[#090a16] text-white',
    cardBg: 'bg-[#12142b]/90 hover:bg-[#181b38]',
    cardBorder: 'border-cyan-500/30 hover:border-cyan-400',
    textColor: 'text-cyan-50',
    subtextColor: 'text-cyan-300/60',
    linkHover: 'hover:scale-[1.015]',
    highlightBorder: 'border-fuchsia-500 ring-2 ring-fuchsia-500/30',
    highlightGlow: 'shadow-[0_0_25px_rgba(217,70,239,0.3)]',
  },
  emerald_forest: {
    bg: 'bg-gradient-to-b from-emerald-950 via-zinc-950 to-zinc-950 text-white',
    cardBg: 'bg-zinc-900/80 hover:bg-emerald-950/40 backdrop-blur-sm',
    cardBorder: 'border-emerald-900/60 hover:border-emerald-500/50',
    textColor: 'text-white',
    subtextColor: 'text-emerald-300/70',
    linkHover: 'hover:scale-[1.015]',
    highlightBorder: 'border-emerald-400 ring-2 ring-emerald-400/20',
    highlightGlow: 'shadow-[0_0_25px_rgba(52,211,153,0.2)]',
  },
};

export default function PublicBioPage() {
  const params = useParams();
  const username = params?.username as string;

  const [bio, setBio] = useState<BioPageDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [copied, setCopied] = useState(false);
  const [showQrModal, setShowQrModal] = useState(false);
  const [pageUrl, setPageUrl] = useState('');

  useEffect(() => {
    if (typeof window !== 'undefined') {
      setPageUrl(window.location.href);
    }

    if (!username) return;

    // Fetch bio data and record view
    const loadBio = async () => {
      setLoading(true);
      try {
        const data = await ApiClient.getPublicBioPage(username);
        setBio(data);
        // Asynchronously record page view
        ApiClient.recordBioPageView(username).catch(() => {});
      } catch (e) {
        console.error('Failed to load bio page', e);
      } finally {
        setLoading(false);
      }
    };

    loadBio();
  }, [username]);

  const handleLinkClick = (link: BioLinkItemDto) => {
    if (link.id) {
      ApiClient.recordBioLinkClick(link.id).catch(() => {});
    }
  };

  const handleCopyProfile = () => {
    if (!pageUrl) return;
    navigator.clipboard.writeText(pageUrl);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-zinc-950 flex flex-col items-center justify-center text-white p-4">
        <div className="w-12 h-12 rounded-2xl bg-zinc-900 border border-zinc-800 flex items-center justify-center animate-spin mb-4">
          <Sparkles className="w-6 h-6 text-white" />
        </div>
        <p className="text-sm font-semibold text-zinc-400 animate-pulse">Bio Sayfası Yükleniyor...</p>
      </div>
    );
  }

  if (!bio) {
    return (
      <div className="min-h-screen bg-zinc-950 flex flex-col items-center justify-center text-white p-4 text-center">
        <div className="w-16 h-16 rounded-3xl bg-zinc-900 border border-zinc-800 flex items-center justify-center mb-4 text-zinc-500">
          <Link2 className="w-8 h-8" />
        </div>
        <h2 className="text-xl font-bold mb-2">Bio Sayfası Bulunamadı</h2>
        <p className="text-sm text-zinc-400 max-w-sm mb-6">
          @{username} adında bir kullanıcı bio profili bulunamadı veya henüz oluşturulmadı.
        </p>
        <Button asChild className="rounded-xl">
          <Link href="/">Kendi Bio Sayfanı Oluştur</Link>
        </Button>
      </div>
    );
  }

  const themeKey = bio.theme && THEME_STYLES[bio.theme] ? bio.theme : 'classic_dark';
  const theme = THEME_STYLES[themeKey];

  // Parse social links JSON
  let socialMap: Record<string, string> = {};
  try {
    if (bio.socialLinks) {
      socialMap = JSON.parse(bio.socialLinks);
    }
  } catch {}

  const activeSocials = Object.entries(socialMap).filter(([_, url]) => url && url.trim().length > 0);

  return (
    <div className={`min-h-screen ${theme.bg} transition-colors duration-300 flex flex-col items-center justify-between p-4 sm:p-6 selection:bg-white/20`}>
      {/* Top Floating Share / QR Bar */}
      <header className="w-full max-w-md flex items-center justify-between py-2 mb-2">
        <Link href="/" className="flex items-center gap-1.5 opacity-75 hover:opacity-100 transition-opacity">
          <div className="w-6 h-6 rounded-lg bg-white/15 backdrop-blur-md flex items-center justify-center">
            <Link2 className="w-3.5 h-3.5" />
          </div>
          <span className="text-xs font-bold tracking-tight">Klink</span>
        </Link>

        <div className="flex items-center gap-2">
          <button
            onClick={() => setShowQrModal(true)}
            className="w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 backdrop-blur-md flex items-center justify-center transition-all cursor-pointer"
            title="QR Kod"
          >
            <QrCode className="w-4 h-4" />
          </button>
          <button
            onClick={handleCopyProfile}
            className="h-8 px-3 rounded-full bg-white/10 hover:bg-white/20 backdrop-blur-md flex items-center gap-1.5 text-xs font-medium transition-all cursor-pointer"
            title="Profili Paylaş"
          >
            {copied ? (
              <>
                <Check className="w-3.5 h-3.5 text-emerald-400" />
                <span>Kopyalandı</span>
              </>
            ) : (
              <>
                <Share2 className="w-3.5 h-3.5" />
                <span>Paylaş</span>
              </>
            )}
          </button>
        </div>
      </header>

      {/* Main Bio Container */}
      <main className="w-full max-w-md flex-1 flex flex-col items-center pt-4 pb-10 space-y-6">
        {/* Avatar Profile Section */}
        <div className="flex flex-col items-center text-center space-y-3">
          <div className="relative">
            {bio.avatarUrl ? (
              <img
                src={bio.avatarUrl}
                alt={bio.displayName}
                className="w-24 h-24 sm:w-28 sm:h-28 rounded-full object-cover shadow-2xl ring-4 ring-white/15"
              />
            ) : (
              <div className="w-24 h-24 sm:w-28 sm:h-28 rounded-full bg-gradient-to-tr from-purple-600 to-indigo-500 flex items-center justify-center text-white text-3xl font-extrabold shadow-2xl ring-4 ring-white/15">
                {(bio.displayName || bio.username).charAt(0).toUpperCase()}
              </div>
            )}

            {bio.verified && (
              <div
                className="absolute bottom-1 right-1 w-6 h-6 rounded-full bg-blue-500 text-white flex items-center justify-center shadow-lg ring-2 ring-zinc-950"
                title="Doğrulanmış Profil"
              >
                <Check className="w-3.5 h-3.5 stroke-[3]" />
              </div>
            )}
          </div>

          <div className="space-y-1">
            <h1 className={`text-xl sm:text-2xl font-extrabold tracking-tight ${theme.textColor} flex items-center justify-center gap-1.5`}>
              <span>{bio.displayName || bio.username}</span>
            </h1>
            <p className={`text-xs font-mono font-medium ${theme.subtextColor}`}>
              @{bio.username}
            </p>
          </div>

          {bio.bioDescription && (
            <p className={`text-xs sm:text-sm max-w-xs font-normal leading-relaxed ${theme.subtextColor}`}>
              {bio.bioDescription}
            </p>
          )}

          {/* Social Icons Row */}
          {activeSocials.length > 0 && (
            <div className="flex items-center justify-center flex-wrap gap-2.5 pt-2">
              {activeSocials.map(([network, url]) => {
                const targetUrl = network === 'email' || network === 'mail' ? `mailto:${url}` : (url.startsWith('http') ? url : `https://${url}`);
                return (
                  <a
                    key={network}
                    href={targetUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="w-9 h-9 rounded-xl bg-white/10 hover:bg-white/20 backdrop-blur-md flex items-center justify-center transition-all hover:scale-110 cursor-pointer shadow-xs"
                    title={network}
                  >
                    {getSocialIcon(network)}
                  </a>
                );
              })}
            </div>
          )}
        </div>

        {/* Links Cards List */}
        <div className="w-full space-y-3 pt-2">
          {bio.links && bio.links.length > 0 ? (
            bio.links.map((link, idx) => {
              const isHighlight = link.highlighted;
              return (
                <a
                  key={link.id || idx}
                  href={link.url}
                  target="_blank"
                  rel="noreferrer"
                  onClick={() => handleLinkClick(link)}
                  className={`
                    w-full block p-4 rounded-2xl border transition-all duration-200 cursor-pointer
                    ${theme.cardBg} ${theme.cardBorder} ${theme.linkHover}
                    ${isHighlight ? `${theme.highlightBorder} ${theme.highlightGlow} animate-pulseSlow` : ''}
                  `}
                >
                  <div className="flex items-center justify-between gap-3">
                    <div className="flex items-center gap-3 min-w-0">
                      <div className="w-9 h-9 rounded-xl bg-white/10 flex items-center justify-center shrink-0">
                        {isHighlight ? (
                          <Flame className="w-4 h-4 text-amber-400 animate-bounce" />
                        ) : (
                          <Globe className="w-4 h-4 opacity-80" />
                        )}
                      </div>
                      <div className="min-w-0">
                        <span className={`text-sm font-bold block truncate ${theme.textColor}`}>
                          {link.title}
                        </span>
                        <span className={`text-[11px] font-mono block truncate opacity-60`}>
                          {link.url.replace(/^https?:\/\//, '')}
                        </span>
                      </div>
                    </div>

                    <div className="w-7 h-7 rounded-lg bg-white/5 flex items-center justify-center shrink-0 opacity-60 group-hover:opacity-100 transition-opacity">
                      <ExternalLink className="w-3.5 h-3.5" />
                    </div>
                  </div>
                </a>
              );
            })
          ) : (
            <div className="p-6 rounded-2xl bg-white/5 border border-white/10 text-center text-xs opacity-60">
              Henüz eklenmiş bir bağlantı bulunmuyor.
            </div>
          )}
        </div>
      </main>

      {/* Footer Branding CTA */}
      <footer className="w-full max-w-md pt-6 pb-4 flex flex-col items-center text-center space-y-3 border-t border-white/10">
        <Link
          href="/"
          className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-white/10 hover:bg-white/20 backdrop-blur-md text-xs font-bold transition-all hover:scale-105 shadow-md"
        >
          <div className="w-4 h-4 rounded-md bg-white text-zinc-950 flex items-center justify-center font-extrabold text-[9px]">
            K
          </div>
          <span>Klink ile Kendi Bio Sayfanı Ücretsiz Oluştur</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </Link>
        <p className="text-[11px] opacity-40">&copy; 2026 Klink. Link Management & Bio Pages.</p>
      </footer>

      {/* QR Code Modal */}
      {showQrModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4 animate-fadeIn">
          <div className="bg-zinc-950 text-white p-6 rounded-3xl border border-zinc-800 max-w-xs w-full text-center space-y-4 shadow-2xl relative">
            <button
              onClick={() => setShowQrModal(false)}
              className="absolute top-4 right-4 text-zinc-400 hover:text-white transition-colors"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="w-12 h-12 rounded-2xl bg-zinc-900 border border-zinc-800 flex items-center justify-center mx-auto text-purple-400">
              <QrCode className="w-6 h-6" />
            </div>

            <div className="space-y-1">
              <h3 className="font-bold text-base">Bio QR Kodu</h3>
              <p className="text-xs text-zinc-400">Bu QR kodu taratarak profilinize anında erişebilirsiniz.</p>
            </div>

            <div className="p-3 bg-white rounded-2xl inline-block shadow-inner">
              <img
                src={`https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=${encodeURIComponent(pageUrl || `http://localhost:3000/bio/${username}`)}`}
                alt="Bio QR Code"
                className="w-[180px] h-[180px] rounded-lg"
              />
            </div>

            <Button
              onClick={handleCopyProfile}
              className="w-full bg-white text-zinc-950 hover:bg-zinc-200 font-bold text-xs h-10 rounded-xl"
            >
              {copied ? 'Bağlantı Kopyalandı!' : 'Bio Linkini Kopyala'}
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
