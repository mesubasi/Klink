'use client';

import React, { useEffect, useState } from 'react';
import { 
  BarChart3, 
  Smartphone, 
  Globe2, 
  Calendar, 
  MapPin, 
  Compass, 
  FileSpreadsheet, 
  FileText, 
  ShieldCheck,
  Zap,
  Mail,
  Flame,
  CheckCircle2,
  Clock
} from 'lucide-react';
import { Language, translations } from '@/lib/translations';
import { AnalyticsSummaryResponse } from '@/lib/types';
import { ApiClient } from '@/lib/api';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';

interface AnalyticsModalProps {
  shortCode: string | null;
  lang: Language;
  authUser: { u: string; p: string };
  onClose: () => void;
}

export const AnalyticsModal: React.FC<AnalyticsModalProps> = ({
  shortCode,
  lang,
  authUser,
  onClose,
}) => {
  const [data, setData] = useState<AnalyticsSummaryResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [emailSending, setEmailSending] = useState(false);
  const [emailSentMessage, setEmailSentMessage] = useState('');
  const [hoveredCell, setHoveredCell] = useState<{ day: string; hour: number; count: number } | null>(null);

  const daysLabelTr = ['Pzt', 'Sal', 'Çar', 'Per', 'Cum', 'Cmt', 'Paz'];
  const daysLabelEn = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
  const days = lang === 'tr' ? daysLabelTr : daysLabelEn;

  useEffect(() => {
    if (!shortCode) return;
    setLoading(true);
    setEmailSentMessage('');
    ApiClient.getAnalyticsSummary(shortCode, lang, authUser)
      .then((res) => setData(res))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, [shortCode, lang, authUser]);

  const t = translations[lang];

  const handleDownloadReport = (format: 'csv' | 'pdf') => {
    if (!shortCode) return;
    const exportUrl = ApiClient.getAnalyticsExportUrl(shortCode, format);
    window.open(exportUrl, '_blank');
  };

  const handleSendEmailReport = async () => {
    if (!shortCode) return;
    setEmailSending(true);
    setEmailSentMessage('');
    try {
      const res = await ApiClient.sendEmailReport(shortCode, undefined, lang, authUser);
      setEmailSentMessage(res.message || (lang === 'tr' ? 'Haftalık rapor e-posta adresinize gönderildi!' : 'Weekly report sent to your email!'));
      setTimeout(() => setEmailSentMessage(''), 5000);
    } catch (e: any) {
      alert(e.message || 'E-posta raporu gönderilirken hata oluştu.');
    } finally {
      setEmailSending(false);
    }
  };

  // Find max value in heatmap for relative color scaling
  const heatmap = data?.hourlyHeatmap || [];
  let maxHeat = 1;
  let peakDay = 0;
  let peakHour = 0;
  let peakCount = 0;

  for (let d = 0; d < heatmap.length; d++) {
    for (let h = 0; h < (heatmap[d]?.length || 0); h++) {
      const val = heatmap[d][h];
      if (val > maxHeat) maxHeat = val;
      if (val > peakCount) {
        peakCount = val;
        peakDay = d;
        peakHour = h;
      }
    }
  }

  const getHeatmapColor = (count: number) => {
    if (!count || count === 0) return 'bg-zinc-100 hover:bg-zinc-200 border-zinc-200/60';
    const ratio = count / maxHeat;
    if (ratio < 0.25) return 'bg-emerald-100 hover:bg-emerald-200 border-emerald-200 text-emerald-900';
    if (ratio < 0.6) return 'bg-emerald-300 hover:bg-emerald-400 border-emerald-400 text-emerald-950';
    if (ratio < 0.85) return 'bg-emerald-500 hover:bg-emerald-600 border-emerald-600 text-white';
    return 'bg-emerald-600 hover:bg-emerald-700 border-emerald-700 text-white font-bold shadow-xs';
  };

  return (
    <Dialog open={!!shortCode} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto p-6 border-zinc-200/90 shadow-2xl">
        <DialogHeader className="border-b border-zinc-100 pb-4">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 pr-6">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-2xl bg-zinc-950 text-white flex items-center justify-center">
                <BarChart3 className="w-5 h-5" />
              </div>
              <div>
                <DialogTitle className="text-base font-bold text-zinc-950">{t.modalAnalyticsTitle}</DialogTitle>
                <DialogDescription className="font-mono text-xs text-zinc-500 mt-0.5">
                  klink.to/{shortCode}
                </DialogDescription>
              </div>
            </div>

            {/* Export & Email Report Actions */}
            <div className="flex items-center gap-1.5 flex-wrap">
              <Button
                variant="outline"
                size="sm"
                onClick={handleSendEmailReport}
                disabled={emailSending}
                className="text-xs h-8 bg-zinc-50 hover:bg-zinc-100 border-zinc-200"
                title="Haftalık Raporu E-posta ile Gönder"
              >
                <Mail className="w-3.5 h-3.5 text-zinc-700 mr-1" />
                <span>{emailSending ? (lang === 'tr' ? 'Gönderiliyor...' : 'Sending...') : (lang === 'tr' ? 'E-posta Raporu' : 'Email Digest')}</span>
              </Button>

              <Button
                variant="outline"
                size="sm"
                onClick={() => handleDownloadReport('csv')}
                className="text-xs h-8"
                title="CSV Formatında İndir"
              >
                <FileSpreadsheet className="w-3.5 h-3.5 text-emerald-600 mr-1" />
                <span>CSV</span>
              </Button>

              <Button
                variant="outline"
                size="sm"
                onClick={() => handleDownloadReport('pdf')}
                className="text-xs h-8"
                title="PDF Formatında İndir"
              >
                <FileText className="w-3.5 h-3.5 text-blue-600 mr-1" />
                <span>PDF</span>
              </Button>
            </div>
          </div>

          {emailSentMessage && (
            <div className="mt-3 p-2.5 rounded-xl bg-emerald-50 border border-emerald-200/80 text-emerald-800 text-xs flex items-center gap-2 animate-fadeIn">
              <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
              <span>{emailSentMessage}</span>
            </div>
          )}
        </DialogHeader>

        {loading || !data ? (
          <div className="py-16 text-center text-zinc-400 text-xs font-medium space-y-2">
            <div className="w-8 h-8 rounded-full border-2 border-zinc-900 border-t-transparent animate-spin mx-auto" />
            <p>{lang === 'tr' ? 'Telemetri verileri yükleniyor...' : 'Loading telemetry data...'}</p>
          </div>
        ) : (
          <div className="space-y-4 text-xs pt-1">
            {/* Total Click Banner */}
            <div className="p-4 rounded-2xl bg-zinc-950 text-white flex items-center justify-between shadow-xs">
              <div>
                <span className="text-[11px] font-semibold text-zinc-400 uppercase tracking-wider">{t.cardTotalClicks}</span>
                <p className="text-3xl font-black font-mono text-white mt-0.5">{data.totalClicks}</p>
              </div>
              <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-zinc-800 border border-zinc-700 text-xs font-mono text-emerald-400">
                <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
                <span>Canlı Teleometri</span>
              </div>
            </div>

            {/* Hourly Click Heatmap Matrix (Saatlik Isı Haritası) */}
            <div className="p-4 rounded-2xl bg-zinc-50 border border-zinc-200/80 space-y-3">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                <div className="flex items-center gap-2 text-zinc-950 font-bold">
                  <Flame className="w-4 h-4 text-orange-600" />
                  <span>{lang === 'tr' ? 'Saatlik & Günlük Tıklama Isı Haritası (Heatmap)' : 'Hourly & Daily Click Heatmap'}</span>
                </div>

                {peakCount > 0 && (
                  <div className="flex items-center gap-1 text-[11px] font-mono text-zinc-600 bg-white px-2.5 py-1 rounded-lg border border-zinc-200 shadow-2xs">
                    <Clock className="w-3 h-3 text-orange-500" />
                    <span>{lang === 'tr' ? 'Zirve:' : 'Peak:'}</span>
                    <strong className="text-zinc-950">{days[peakDay]} {String(peakHour).padStart(2, '0')}:00</strong>
                    <span className="text-emerald-600 font-bold">({peakCount} tık)</span>
                  </div>
                )}
              </div>

              {/* Heatmap Grid */}
              <div className="overflow-x-auto pb-1">
                <div className="min-w-[560px] space-y-1">
                  {/* Hours Header Row */}
                  <div className="grid grid-cols-[40px_repeat(24,1fr)] gap-1 text-[9px] text-zinc-400 font-mono text-center mb-1">
                    <div />
                    {Array.from({ length: 24 }).map((_, h) => (
                      <div key={h} className="truncate">
                        {h % 3 === 0 ? `${h}` : '·'}
                      </div>
                    ))}
                  </div>

                  {/* 7 Days Rows */}
                  {days.map((dayName, dayIdx) => (
                    <div key={dayName} className="grid grid-cols-[40px_repeat(24,1fr)] gap-1 items-center">
                      <span className="text-[10px] font-semibold text-zinc-500">{dayName}</span>
                      {Array.from({ length: 24 }).map((_, hourIdx) => {
                        const count = heatmap[dayIdx]?.[hourIdx] || 0;
                        return (
                          <div
                            key={hourIdx}
                            onMouseEnter={() => setHoveredCell({ day: dayName, hour: hourIdx, count })}
                            onMouseLeave={() => setHoveredCell(null)}
                            className={`h-4 rounded-xs border transition-all cursor-pointer ${getHeatmapColor(count)}`}
                            title={`${dayName} ${String(hourIdx).padStart(2, '0')}:00 — ${count} ${lang === 'tr' ? 'Tıklama' : 'Clicks'}`}
                          />
                        );
                      })}
                    </div>
                  ))}
                </div>
              </div>

              {/* Dynamic Cell Tooltip / Legend */}
              <div className="flex items-center justify-between pt-1 border-t border-zinc-200/60 text-[11px] text-zinc-500">
                <div className="min-h-[16px] font-mono">
                  {hoveredCell ? (
                    <span className="text-zinc-900 font-semibold">
                      📍 {hoveredCell.day} {String(hoveredCell.hour).padStart(2, '0')}:00 – {String(hoveredCell.hour + 1).padStart(2, '0')}:00: <strong className="text-emerald-600 font-bold">{hoveredCell.count}</strong> {lang === 'tr' ? 'tıklama' : 'clicks'}
                    </span>
                  ) : (
                    <span>{lang === 'tr' ? 'Detay için hücrelerin üzerine gelin' : 'Hover over cells for hourly breakdown'}</span>
                  )}
                </div>

                <div className="flex items-center gap-1.5 text-[10px] text-zinc-400">
                  <span>{lang === 'tr' ? 'Az' : 'Less'}</span>
                  <div className="w-2.5 h-2.5 rounded-2xs bg-zinc-100 border border-zinc-200" />
                  <div className="w-2.5 h-2.5 rounded-2xs bg-emerald-100 border border-emerald-200" />
                  <div className="w-2.5 h-2.5 rounded-2xs bg-emerald-300 border border-emerald-400" />
                  <div className="w-2.5 h-2.5 rounded-2xs bg-emerald-600 border border-emerald-700" />
                  <span>{lang === 'tr' ? 'Çok' : 'More'}</span>
                </div>
              </div>
            </div>

            {/* Bot vs Human Traffic Quality Panel */}
            {(data.humanClicks !== undefined || data.botClicks !== undefined) && (
              <div className="p-4 rounded-2xl bg-zinc-50 border border-zinc-200/80 space-y-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2 text-zinc-950 font-bold">
                    <ShieldCheck className="w-4 h-4 text-emerald-600" />
                    <span>{t.trafficQuality}</span>
                  </div>
                  <Badge variant="secondary" className="font-mono text-[10px]">
                    RabbitMQ Telemetrisi
                  </Badge>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  {/* Human Clicks */}
                  <div className="p-3 rounded-xl bg-white border border-zinc-200/80 text-center">
                    <p className="text-[10px] text-zinc-500 uppercase tracking-wider font-semibold mb-0.5">{t.humanClicks}</p>
                    <p className="text-2xl font-black text-emerald-600 font-mono">{data.humanClicks ?? 0}</p>
                    <p className="text-[10px] text-zinc-400 font-mono mt-0.5">
                      {((data.humanClicks ?? 0) + (data.botClicks ?? 0)) > 0
                        ? `${Math.round(((data.humanClicks ?? 0) / ((data.humanClicks ?? 0) + (data.botClicks ?? 0))) * 100)}% Gerçek Kullanıcı`
                        : '—'}
                    </p>
                  </div>
                  {/* Bot Clicks */}
                  <div className="p-3 rounded-xl bg-white border border-zinc-200/80 text-center">
                    <p className="text-[10px] text-zinc-500 uppercase tracking-wider font-semibold mb-0.5">{t.botClicks}</p>
                    <p className="text-2xl font-black text-zinc-700 font-mono">{data.botClicks ?? 0}</p>
                    <p className="text-[10px] text-zinc-400 font-mono mt-0.5">
                      {((data.humanClicks ?? 0) + (data.botClicks ?? 0)) > 0
                        ? `${Math.round(((data.botClicks ?? 0) / ((data.humanClicks ?? 0) + (data.botClicks ?? 0))) * 100)}% Crawler / Bot`
                        : '—'}
                    </p>
                  </div>
                </div>

                {/* Combined Progress Meter */}
                <div className="w-full h-2 bg-zinc-200 rounded-full overflow-hidden flex">
                  <div
                    className="h-full bg-emerald-500 transition-all duration-300"
                    style={{
                      width: `${((data.humanClicks ?? 0) + (data.botClicks ?? 0)) > 0
                        ? ((data.humanClicks ?? 0) / ((data.humanClicks ?? 0) + (data.botClicks ?? 0))) * 100
                        : 100}%`,
                    }}
                  />
                  <div
                    className="h-full bg-zinc-400 transition-all duration-300"
                    style={{
                      width: `${((data.humanClicks ?? 0) + (data.botClicks ?? 0)) > 0
                        ? ((data.botClicks ?? 0) / ((data.humanClicks ?? 0) + (data.botClicks ?? 0))) * 100
                        : 0}%`,
                    }}
                  />
                </div>
              </div>
            )}

            {/* Devices & Referrers Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {/* Devices */}
              <div className="p-4 rounded-2xl bg-zinc-50 border border-zinc-200/80 space-y-3">
                <div className="flex items-center gap-2 text-zinc-950 font-bold">
                  <Smartphone className="w-4 h-4 text-zinc-700" />
                  <span>{t.deviceDistribution}</span>
                </div>
                <div className="space-y-2">
                  {Object.entries(data.clicksByDevice || {}).map(([device, count]) => (
                    <div key={device} className="space-y-1">
                      <div className="flex justify-between text-[11px] text-zinc-700 font-medium">
                        <span>{device}</span>
                        <span className="font-mono font-bold text-zinc-950">{count}</span>
                      </div>
                      <div className="w-full h-1.5 bg-zinc-200 rounded-full overflow-hidden">
                        <div
                          className="h-full bg-zinc-900 rounded-full"
                          style={{
                            width: `${Math.min(100, (count / (data.totalClicks || 1)) * 100)}%`,
                          }}
                        />
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Referrers */}
              <div className="p-4 rounded-2xl bg-zinc-50 border border-zinc-200/80 space-y-3">
                <div className="flex items-center gap-2 text-zinc-950 font-bold">
                  <Globe2 className="w-4 h-4 text-zinc-700" />
                  <span>{t.topReferrers}</span>
                </div>
                <div className="space-y-2">
                  {Object.entries(data.clicksByReferrer || {}).map(([ref, count]) => (
                    <div key={ref} className="space-y-1">
                      <div className="flex justify-between text-[11px] text-zinc-700 font-medium">
                        <span className="truncate max-w-[150px]">{ref}</span>
                        <span className="font-mono font-bold text-zinc-950">{count}</span>
                      </div>
                      <div className="w-full h-1.5 bg-zinc-200 rounded-full overflow-hidden">
                        <div
                          className="h-full bg-zinc-900 rounded-full"
                          style={{
                            width: `${Math.min(100, (count / (data.totalClicks || 1)) * 100)}%`,
                          }}
                        />
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Geo-IP Country & City Breakdown */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {/* Countries */}
              <div className="p-4 rounded-2xl bg-zinc-50 border border-zinc-200/80 space-y-3">
                <div className="flex items-center gap-2 text-zinc-950 font-bold">
                  <Compass className="w-4 h-4 text-zinc-700" />
                  <span>{lang === 'tr' ? 'Ülke Dağılımı' : 'Country Breakdown'}</span>
                </div>
                <div className="space-y-2">
                  {Object.entries(data.clicksByCountry || { 'Türkiye (TR)': 1 }).map(([country, count]) => (
                    <div key={country} className="space-y-1">
                      <div className="flex justify-between text-[11px] text-zinc-700 font-medium">
                        <span className="truncate max-w-[150px]">{country}</span>
                        <span className="font-mono font-bold text-zinc-950">{count}</span>
                      </div>
                      <div className="w-full h-1.5 bg-zinc-200 rounded-full overflow-hidden">
                        <div
                          className="h-full bg-zinc-900 rounded-full"
                          style={{
                            width: `${Math.min(100, (count / (data.totalClicks || 1)) * 100)}%`,
                          }}
                        />
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Cities */}
              <div className="p-4 rounded-2xl bg-zinc-50 border border-zinc-200/80 space-y-3">
                <div className="flex items-center gap-2 text-zinc-950 font-bold">
                  <MapPin className="w-4 h-4 text-zinc-700" />
                  <span>{lang === 'tr' ? 'Şehir Dağılımı' : 'City Breakdown'}</span>
                </div>
                <div className="space-y-2">
                  {Object.entries(data.clicksByCity || { 'İstanbul': 1 }).map(([city, count]) => (
                    <div key={city} className="space-y-1">
                      <div className="flex justify-between text-[11px] text-zinc-700 font-medium">
                        <span className="truncate max-w-[150px]">{city}</span>
                        <span className="font-mono font-bold text-zinc-950">{count}</span>
                      </div>
                      <div className="w-full h-1.5 bg-zinc-200 rounded-full overflow-hidden">
                        <div
                          className="h-full bg-zinc-900 rounded-full"
                          style={{
                            width: `${Math.min(100, (count / (data.totalClicks || 1)) * 100)}%`,
                          }}
                        />
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Daily Date Breakdown */}
            <div className="p-4 rounded-2xl bg-zinc-50 border border-zinc-200/80 space-y-3">
              <div className="flex items-center gap-2 text-zinc-950 font-bold">
                <Calendar className="w-4 h-4 text-zinc-700" />
                <span>{lang === 'tr' ? 'Günlük Tıklama Geçmişi' : 'Daily Click Timeline'}</span>
              </div>
              <div className="flex items-center gap-2 overflow-x-auto pb-1">
                {Object.entries(data.clicksByDate || {}).map(([date, count]) => (
                  <div
                    key={date}
                    className="px-3 py-2 rounded-xl bg-white border border-zinc-200/80 text-center min-w-[90px]"
                  >
                    <p className="text-[10px] text-zinc-400 font-mono">{date}</p>
                    <p className="text-sm font-bold text-zinc-950 font-mono mt-0.5">{count}</p>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
};

