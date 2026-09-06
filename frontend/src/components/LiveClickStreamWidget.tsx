'use client';

import React, { useState, useEffect, useRef } from 'react';
import { 
  Radio, 
  Wifi, 
  WifiOff, 
  Volume2, 
  VolumeX, 
  Pause, 
  Play, 
  Trash2, 
  ExternalLink, 
  Globe, 
  Smartphone, 
  Monitor, 
  Bot, 
  Navigation, 
  ShieldAlert, 
  Filter, 
  Layers,
  Sparkles,
  Clock
} from 'lucide-react';
import { LiveClickDto } from '@/lib/types';
import { ApiClient } from '@/lib/api';
import { Language, translations } from '@/lib/translations';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';

interface LiveClickStreamWidgetProps {
  lang?: Language;
  authUser: { u: string; p: string; token?: string; role?: string } | null;
  filterShortCode?: string;
  compact?: boolean;
}

export const LiveClickStreamWidget: React.FC<LiveClickStreamWidgetProps> = ({
  lang = 'tr',
  authUser,
  filterShortCode,
  compact = false
}) => {
  const [clicks, setClicks] = useState<LiveClickDto[]>([]);
  const [connectionStatus, setConnectionStatus] = useState<'connecting' | 'connected' | 'disconnected'>('connecting');
  const [isPaused, setIsPaused] = useState(false);
  const [soundEnabled, setSoundEnabled] = useState(false);
  const [filterType, setFilterType] = useState<'all' | 'human' | 'bot'>('all');
  const [pulseCount, setPulseCount] = useState(0);

  const eventSourceRef = useRef<EventSource | null>(null);
  const isPausedRef = useRef(isPaused);
  isPausedRef.current = isPaused;

  const audioCtxRef = useRef<AudioContext | null>(null);

  // Web Audio API ile yumuşak ping sesi çalma (Harici ses dosyası gerektirmez)
  const playPingSound = () => {
    try {
      if (typeof window === 'undefined') return;
      const AudioCtx = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
      if (!AudioCtx) return;

      if (!audioCtxRef.current) {
        audioCtxRef.current = new AudioCtx();
      }

      if (audioCtxRef.current.state === 'suspended') {
        audioCtxRef.current.resume();
      }

      const ctx = audioCtxRef.current;
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();

      osc.type = 'sine';
      osc.frequency.setValueAtTime(880, ctx.currentTime); // A5 tonu
      osc.frequency.exponentialRampToValueAtTime(1320, ctx.currentTime + 0.1);

      gain.gain.setValueAtTime(0.04, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.2);

      osc.connect(gain);
      gain.connect(ctx.destination);

      osc.start();
      osc.stop(ctx.currentTime + 0.2);
    } catch {
      // Ses çalma hatası tarayıcı autoplay kısıtlamasından kaynaklanabilir, sessizce yut
    }
  };

  useEffect(() => {
    if (!authUser) return;

    // 1. Önce bellekteki son tıklama geçmişini REST ile çek
    ApiClient.getRecentClicks(lang, authUser, filterShortCode)
      .then((recents) => {
        if (recents && recents.length > 0) {
          setClicks(recents);
        }
      })
      .catch(() => {});

    // 2. SSE Bağlantısını Kur
    const streamUrl = ApiClient.getTelemetryStreamUrl(authUser.token, filterShortCode);
    setConnectionStatus('connecting');

    const es = new EventSource(streamUrl);
    eventSourceRef.current = es;

    es.addEventListener('connected', () => {
      setConnectionStatus('connected');
    });

    es.addEventListener('init', (e: MessageEvent) => {
      try {
        const initialData: LiveClickDto[] = JSON.parse(e.data);
        if (Array.isArray(initialData)) {
          setClicks(initialData);
        }
      } catch (err) {
        console.error('SSE init parsing hatası:', err);
      }
    });

    es.addEventListener('click', (e: MessageEvent) => {
      try {
        const newClick: LiveClickDto = JSON.parse(e.data);

        // Canlı nabız efektini tetikle
        setPulseCount((prev) => prev + 1);

        if (soundEnabled) {
          playPingSound();
        }

        if (!isPausedRef.current) {
          setClicks((prev) => [newClick, ...prev.slice(0, 49)]);
        }
      } catch (err) {
        console.error('SSE click parsing hatası:', err);
      }
    });

    es.onopen = () => {
      setConnectionStatus('connected');
    };

    es.onerror = () => {
      // EventSource varsayılan olarak otomatik yeniden bağlanmayı dener
      setConnectionStatus('disconnected');
    };

    return () => {
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
        eventSourceRef.current = null;
      }
    };
  }, [authUser, filterShortCode, soundEnabled, lang]);

  const filteredClicks = clicks.filter((c) => {
    if (filterType === 'human') return !c.bot;
    if (filterType === 'bot') return c.bot;
    return true;
  });

  const humanCount = clicks.filter((c) => !c.bot).length;
  const botCount = clicks.filter((c) => c.bot).length;

  const formatRelativeTime = (timestamp: number) => {
    const diff = Math.max(0, Math.floor((Date.now() - timestamp) / 1000));
    if (diff < 5) return lang === 'tr' ? 'Az önce' : 'Just now';
    if (diff < 60) return `${diff}s ${lang === 'tr' ? 'önce' : 'ago'}`;
    const mins = Math.floor(diff / 60);
    if (mins < 60) return `${mins}dk ${lang === 'tr' ? 'önce' : 'm ago'}`;
    const hours = Math.floor(mins / 60);
    return `${hours}sa ${lang === 'tr' ? 'önce' : 'h ago'}`;
  };

  const getDeviceIcon = (deviceType: string, isBot: boolean) => {
    if (isBot) return <Bot className="w-3.5 h-3.5 text-amber-500" />;
    if (deviceType.toLowerCase().includes('mobil') || deviceType.toLowerCase().includes('mobile')) {
      return <Smartphone className="w-3.5 h-3.5 text-blue-500" />;
    }
    return <Monitor className="w-3.5 h-3.5 text-emerald-500" />;
  };

  return (
    <div className={`rounded-2xl bg-white dark:bg-zinc-900 border border-zinc-200/90 dark:border-zinc-800 shadow-2xs overflow-hidden ${compact ? 'p-4' : 'p-5 sm:p-6'}`}>
      {/* Header & Status Bar */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 pb-4 border-b border-zinc-100 dark:border-zinc-800">
        <div className="flex items-center gap-3">
          <div className="relative flex items-center justify-center">
            <span className="relative flex h-3 w-3">
              {connectionStatus === 'connected' && !isPaused && (
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
              )}
              <span className={`relative inline-flex rounded-full h-3 w-3 ${
                connectionStatus === 'connected' 
                  ? (isPaused ? 'bg-amber-400' : 'bg-emerald-500') 
                  : (connectionStatus === 'connecting' ? 'bg-amber-500' : 'bg-rose-500')
              }`}></span>
            </span>
          </div>

          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-base font-bold text-zinc-950 dark:text-white flex items-center gap-1.5">
                <Radio className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
                <span>{lang === 'tr' ? 'Canlı Ziyaret Akışı' : 'Live Click Stream'}</span>
              </h3>
              
              <Badge variant="outline" className="text-[10px] font-mono uppercase tracking-wider py-0 px-1.5 dark:border-zinc-700">
                SSE Stream
              </Badge>

              {filterShortCode && (
                <Badge variant="secondary" className="text-xs font-mono">
                  /{filterShortCode}
                </Badge>
              )}
            </div>
            <p className="text-xs text-zinc-500 dark:text-zinc-400 mt-0.5">
              {connectionStatus === 'connected' 
                ? (isPaused 
                    ? (lang === 'tr' ? 'Akış geçici olarak duraklatıldı' : 'Stream paused') 
                    : (lang === 'tr' ? 'Gerçek zamanlı bağlantı aktif — dinleniyor' : 'Real-time telemetry active — listening'))
                : (connectionStatus === 'connecting' 
                    ? (lang === 'tr' ? 'Sunucuya bağlanılıyor...' : 'Connecting to stream...') 
                    : (lang === 'tr' ? 'Bağlantı koptu, yeniden deneniyor...' : 'Disconnected, retrying...'))}
            </p>
          </div>
        </div>

        {/* Action Controls & Toggles */}
        <div className="flex items-center gap-1.5 flex-wrap self-end sm:self-auto">
          {/* Pause / Resume Button */}
          <Button
            variant="outline"
            size="sm"
            onClick={() => setIsPaused(!isPaused)}
            className={`h-8 text-xs font-medium cursor-pointer ${
              isPaused 
                ? 'bg-amber-50 dark:bg-amber-950/30 text-amber-700 dark:text-amber-400 border-amber-300 dark:border-amber-700' 
                : 'dark:bg-zinc-800 dark:border-zinc-700'
            }`}
            title={isPaused ? 'Akışı Devam Ettir' : 'Akışı Duraklat'}
          >
            {isPaused ? <Play className="w-3.5 h-3.5 mr-1 text-amber-600" /> : <Pause className="w-3.5 h-3.5 mr-1" />}
            <span>{isPaused ? (lang === 'tr' ? 'Devam Et' : 'Resume') : (lang === 'tr' ? 'Duraklat' : 'Pause')}</span>
          </Button>

          {/* Sound Toggle */}
          <Button
            variant="outline"
            size="sm"
            onClick={() => setSoundEnabled(!soundEnabled)}
            className={`h-8 px-2.5 text-xs font-medium cursor-pointer ${
              soundEnabled 
                ? 'bg-emerald-50 dark:bg-emerald-950/30 text-emerald-700 dark:text-emerald-400 border-emerald-300 dark:border-emerald-700' 
                : 'dark:bg-zinc-800 dark:border-zinc-700 text-zinc-500'
            }`}
            title={soundEnabled ? 'Sesli Bildirimi Kapat' : 'Yeni Tıklamada Ses Çal'}
          >
            {soundEnabled ? <Volume2 className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400" /> : <VolumeX className="w-3.5 h-3.5" />}
          </Button>

          {/* Filter Segmented Buttons */}
          <div className="flex items-center rounded-lg border border-zinc-200 dark:border-zinc-700 p-0.5 bg-zinc-50 dark:bg-zinc-800/50">
            <button
              onClick={() => setFilterType('all')}
              className={`px-2 py-1 text-[11px] font-medium rounded-md transition-colors cursor-pointer ${
                filterType === 'all' 
                  ? 'bg-white dark:bg-zinc-900 text-zinc-900 dark:text-white shadow-2xs font-semibold' 
                  : 'text-zinc-500 hover:text-zinc-900 dark:hover:text-white'
              }`}
            >
              {lang === 'tr' ? 'Tümü' : 'All'} ({clicks.length})
            </button>
            <button
              onClick={() => setFilterType('human')}
              className={`px-2 py-1 text-[11px] font-medium rounded-md transition-colors cursor-pointer ${
                filterType === 'human' 
                  ? 'bg-white dark:bg-zinc-900 text-emerald-600 dark:text-emerald-400 shadow-2xs font-semibold' 
                  : 'text-zinc-500 hover:text-zinc-900 dark:hover:text-white'
              }`}
            >
              {lang === 'tr' ? 'İnsan' : 'Human'} ({humanCount})
            </button>
            <button
              onClick={() => setFilterType('bot')}
              className={`px-2 py-1 text-[11px] font-medium rounded-md transition-colors cursor-pointer ${
                filterType === 'bot' 
                  ? 'bg-white dark:bg-zinc-900 text-amber-600 dark:text-amber-400 shadow-2xs font-semibold' 
                  : 'text-zinc-500 hover:text-zinc-900 dark:hover:text-white'
              }`}
            >
              Bot ({botCount})
            </button>
          </div>

          {/* Clear Feed */}
          {clicks.length > 0 && (
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setClicks([])}
              className="h-8 px-2 text-zinc-400 hover:text-rose-600 dark:hover:text-rose-400 cursor-pointer"
              title="Akışı Temizle"
            >
              <Trash2 className="w-3.5 h-3.5" />
            </Button>
          )}
        </div>
      </div>

      {/* Stream List / Items */}
      <div className="mt-4">
        {filteredClicks.length === 0 ? (
          <div className="py-12 px-4 text-center rounded-xl border border-dashed border-zinc-200 dark:border-zinc-800 space-y-2">
            <Radio className="w-8 h-8 mx-auto text-zinc-300 dark:text-zinc-600 animate-pulse" />
            <p className="text-sm font-medium text-zinc-600 dark:text-zinc-300">
              {lang === 'tr' ? 'Canlı tıklamalar bekleniyor...' : 'Waiting for incoming live clicks...'}
            </p>
            <p className="text-xs text-zinc-400 max-w-sm mx-auto">
              {lang === 'tr'
                ? 'Kısa linklerinizden herhangi biri tıklandığında anında burada görüntülenecektir.'
                : 'Whenever any of your short links is visited, it will appear here in real time.'}
            </p>
          </div>
        ) : (
          <div className="space-y-2 max-h-[420px] overflow-y-auto pr-1">
            {filteredClicks.map((item, idx) => (
              <div
                key={`${item.shortCode}-${item.clickedAt}-${idx}`}
                className="group p-3 rounded-xl bg-zinc-50/70 hover:bg-zinc-100/80 dark:bg-zinc-800/40 dark:hover:bg-zinc-800/70 border border-zinc-200/60 dark:border-zinc-800/60 transition-all duration-200 flex flex-col sm:flex-row sm:items-center justify-between gap-2.5"
              >
                {/* Left Block: Location, Link & Time */}
                <div className="flex items-start sm:items-center gap-3">
                  <div className="w-8 h-8 rounded-lg bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-700 flex items-center justify-center shrink-0 shadow-2xs">
                    {getDeviceIcon(item.deviceType, item.bot)}
                  </div>

                  <div className="space-y-0.5 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="text-xs font-semibold text-zinc-900 dark:text-zinc-100 flex items-center gap-1">
                        <Globe className="w-3.5 h-3.5 text-zinc-400" />
                        <span>{item.city ? `${item.city}, ` : ''}{item.country || 'Bilinmiyor'}</span>
                      </span>

                      <span className="text-zinc-300 dark:text-zinc-700">•</span>

                      <span className="text-xs font-mono font-bold text-emerald-600 dark:text-emerald-400">
                        /{item.shortCode}
                      </span>

                      {item.bot && (
                        <Badge variant="outline" className="text-[10px] bg-amber-50 dark:bg-amber-950/40 text-amber-700 dark:text-amber-400 border-amber-200 dark:border-amber-800">
                          {item.botCategory || 'Bot'}
                        </Badge>
                      )}

                      {item.variantLabel && (
                        <Badge variant="outline" className="text-[10px] bg-purple-50 dark:bg-purple-950/40 text-purple-700 dark:text-purple-400 border-purple-200 dark:border-purple-800">
                          A/B: {item.variantLabel}
                        </Badge>
                      )}
                    </div>

                    <div className="flex items-center gap-2 text-[11px] text-zinc-500 dark:text-zinc-400 truncate max-w-md">
                      <span className="truncate" title={item.originalUrl}>
                        {item.originalUrl}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Right Block: Telemetry Badges & Relative Time */}
                <div className="flex items-center gap-2 self-end sm:self-auto shrink-0 flex-wrap">
                  <span className="text-[11px] font-mono text-zinc-400 bg-white dark:bg-zinc-900 px-2 py-0.5 rounded border border-zinc-200/80 dark:border-zinc-700/80">
                    {item.browser} • {item.os}
                  </span>

                  <span className="text-[11px] font-mono text-zinc-400 hidden md:inline">
                    {item.maskedIp}
                  </span>

                  <span className="text-[11px] font-medium text-zinc-500 dark:text-zinc-400 flex items-center gap-1 bg-zinc-200/60 dark:bg-zinc-700/60 px-2 py-0.5 rounded-full">
                    <Clock className="w-3 h-3 text-zinc-400" />
                    <span>{formatRelativeTime(item.clickedAt)}</span>
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Footer Info */}
      <div className="mt-3 pt-3 border-t border-zinc-100 dark:border-zinc-800/80 flex items-center justify-between text-[11px] text-zinc-400">
        <div className="flex items-center gap-1.5">
          <Sparkles className="w-3.5 h-3.5 text-emerald-500" />
          <span>{lang === 'tr' ? 'RabbitMQ Event Streaming & Spring SseEmitter ile güçlendirildi' : 'Powered by RabbitMQ Event Streaming & Spring SseEmitter'}</span>
        </div>
        <div className="font-mono">
          {filteredClicks.length} {lang === 'tr' ? 'kayıt' : 'events'}
        </div>
      </div>
    </div>
  );
};
