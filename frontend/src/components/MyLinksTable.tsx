'use client';

import React, { useState } from 'react';
import { 
  Search, 
  Lock, 
  QrCode, 
  BarChart3, 
  Copy, 
  Check, 
  Trash2, 
  ExternalLink, 
  ShieldCheck, 
  Eye, 
  Globe,
  Filter,
  ArrowUpDown,
  Sparkles,
  Link2,
  Smartphone,
  Webhook
} from 'lucide-react';
import { Language, translations } from '@/lib/translations';
import { ShortenResponse } from '@/lib/types';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';

interface MyLinksTableProps {
  lang: Language;
  links: ShortenResponse[];
  onToggleStatus: (shortCode: string, currentActive: boolean) => void;
  onOpenQr: (shortCode: string) => void;
  onOpenPasswordModal: (shortCode: string) => void;
  onOpenAnalyticsModal: (shortCode: string) => void;
  onDeleteLink: (shortCode: string) => void;
}

export const MyLinksTable: React.FC<MyLinksTableProps> = ({
  lang,
  links,
  onOpenQr,
  onOpenPasswordModal,
  onOpenAnalyticsModal,
  onDeleteLink,
}) => {
  const t = translations[lang];

  const [searchQuery, setSearchQuery] = useState('');
  const [filterType, setFilterType] = useState<'all' | 'protected' | 'preview'>('all');
  const [copiedCode, setCopiedCode] = useState<string | null>(null);

  const getDomainName = (url: string) => {
    try {
      const parsed = new URL(url.startsWith('http') ? url : `https://${url}`);
      return parsed.hostname.replace('www.', '');
    } catch {
      return 'link';
    }
  };

  const filteredLinks = links
    .filter((link) => {
      const matchesSearch =
        link.shortCode.toLowerCase().includes(searchQuery.toLowerCase()) ||
        link.originalUrl.toLowerCase().includes(searchQuery.toLowerCase());
      
      if (!matchesSearch) return false;
      if (filterType === 'protected') return !!link.passwordProtected;
      if (filterType === 'preview') return !!link.previewEnabled;
      return true;
    });

  const handleCopy = (shortUrl: string, shortCode: string) => {
    navigator.clipboard.writeText(shortUrl);
    setCopiedCode(shortCode);
    setTimeout(() => setCopiedCode(null), 2000);
  };

  const maxClicks = Math.max(...links.map((l) => l.clickCount || 0), 1);

  return (
    <Card className="border-zinc-200/90 shadow-sm overflow-hidden">
      <CardHeader className="border-b border-zinc-100 pb-4 bg-zinc-50/40">
        <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2">
              <CardTitle className="text-base font-bold text-zinc-950">{t.myLinksTitle}</CardTitle>
              <Badge variant="secondary" className="font-mono text-xs">
                {filteredLinks.length}
              </Badge>
            </div>
            <CardDescription className="text-xs mt-0.5 text-zinc-500">
              {lang === 'tr' ? 'Hesabınızdaki tüm aktif bağlantılar ve tıklama kayıtları' : 'All active shortened links and telemetry'}
            </CardDescription>
          </div>

          {/* Search Bar & Filter Pills */}
          <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-2.5 w-full md:w-auto">
            {/* Filter Pills */}
            <div className="flex items-center p-0.5 rounded-lg bg-zinc-100 border border-zinc-200/80 text-[11px] font-semibold">
              <button
                type="button"
                onClick={() => setFilterType('all')}
                className={`px-2.5 py-1 rounded-md transition-all cursor-pointer ${
                  filterType === 'all' ? 'bg-white text-zinc-950 shadow-2xs font-bold' : 'text-zinc-500 hover:text-zinc-900'
                }`}
              >
                {lang === 'tr' ? 'Tümü' : 'All'}
              </button>
              <button
                type="button"
                onClick={() => setFilterType('protected')}
                className={`px-2.5 py-1 rounded-md transition-all cursor-pointer ${
                  filterType === 'protected' ? 'bg-white text-zinc-950 shadow-2xs font-bold' : 'text-zinc-500 hover:text-zinc-900'
                }`}
              >
                {lang === 'tr' ? 'Şifreli' : 'Protected'}
              </button>
              <button
                type="button"
                onClick={() => setFilterType('preview')}
                className={`px-2.5 py-1 rounded-md transition-all cursor-pointer ${
                  filterType === 'preview' ? 'bg-white text-zinc-950 shadow-2xs font-bold' : 'text-zinc-500 hover:text-zinc-900'
                }`}
              >
                {lang === 'tr' ? 'Kalkanlı' : 'Shield'}
              </button>
            </div>

            {/* Search Input */}
            <div className="relative w-full sm:w-60">
              <div className="absolute inset-y-0 left-0 pl-2.5 flex items-center pointer-events-none text-zinc-400">
                <Search className="w-3.5 h-3.5" />
              </div>
              <Input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder={t.searchPlaceholder}
                className="pl-8 h-8 text-xs bg-white"
              />
            </div>
          </div>
        </div>
      </CardHeader>

      <CardContent className="p-0">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>{t.colCode}</TableHead>
              <TableHead>{t.colTarget}</TableHead>
              <TableHead>{t.colClicks}</TableHead>
              <TableHead>{t.colStatus}</TableHead>
              <TableHead>{t.colCreated}</TableHead>
              <TableHead className="text-right">{t.colActions}</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredLinks.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="h-32 text-center text-zinc-400 font-medium">
                  <div className="flex flex-col items-center justify-center space-y-2">
                    <div className="w-10 h-10 rounded-2xl bg-zinc-100 flex items-center justify-center text-zinc-400">
                      <Link2 className="w-5 h-5" />
                    </div>
                    <p className="text-xs text-zinc-600 font-semibold">
                      {lang === 'tr' ? 'Kayıtlı bir kısa bağlantı bulunamadı.' : 'No short links found.'}
                    </p>
                    <p className="text-[11px] text-zinc-400">
                      {lang === 'tr' ? 'Yukarıdaki alandan ilk linkinizi anında oluşturabilirsiniz.' : 'Create your first short link from the panel above.'}
                    </p>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              filteredLinks.map((link) => {
                const domain = getDomainName(link.originalUrl);
                const clickPercent = Math.min(100, Math.round(((link.clickCount || 0) / maxClicks) * 100));

                return (
                  <TableRow key={link.shortCode} className="group hover:bg-zinc-50/70 transition-colors">
                    {/* Short Code & Instant Copy */}
                    <TableCell>
                      <div className="flex items-center gap-2">
                        <div className="w-7 h-7 rounded-lg bg-zinc-900 text-white flex items-center justify-center shrink-0">
                          <Link2 className="w-3.5 h-3.5" />
                        </div>
                        <div>
                          <div className="flex items-center gap-1.5">
                            <span className="font-bold text-zinc-950 font-mono text-xs">
                              {link.shortCode}
                            </span>
                            <button
                              onClick={() => handleCopy(link.shortUrl, link.shortCode)}
                              className="text-zinc-400 hover:text-zinc-950 transition-colors p-1 rounded hover:bg-zinc-200/80 cursor-pointer"
                              title={t.btnCopy}
                            >
                              {copiedCode === link.shortCode ? (
                                <Check className="w-3.5 h-3.5 text-emerald-600" />
                              ) : (
                                <Copy className="w-3.5 h-3.5" />
                              )}
                            </button>
                          </div>
                          <span className="text-[10px] text-zinc-400 font-mono truncate max-w-[150px] block">
                            {link.shortUrl}
                          </span>
                        </div>
                      </div>
                    </TableCell>

                    {/* Target Destination URL with Domain Badge */}
                    <TableCell className="max-w-xs truncate">
                      <div className="space-y-0.5">
                        <div className="flex items-center gap-1.5">
                          <span className="inline-flex items-center px-1.5 py-0.2 rounded text-[10px] font-mono font-medium bg-zinc-100 text-zinc-700 border border-zinc-200/70">
                            <Globe className="w-2.5 h-2.5 mr-1 text-zinc-400" />
                            {domain}
                          </span>
                        </div>
                        <a
                          href={link.originalUrl}
                          target="_blank"
                          rel="noreferrer"
                          className="text-zinc-600 hover:text-zinc-950 flex items-center gap-1 text-xs truncate hover:underline"
                        >
                          <span className="truncate">{link.originalUrl}</span>
                          <ExternalLink className="w-3 h-3 text-zinc-400 shrink-0" />
                        </a>
                      </div>
                    </TableCell>

                    {/* Clicks & Popularity Bar */}
                    <TableCell>
                      <div className="space-y-1 w-24">
                        <div className="flex items-center justify-between text-xs">
                          <span className="font-mono font-bold text-zinc-950">{link.clickCount}</span>
                          <span className="text-[10px] text-zinc-400">tık</span>
                        </div>
                        <div className="w-full h-1 bg-zinc-100 rounded-full overflow-hidden">
                          <div
                            className="h-full bg-zinc-900 rounded-full"
                            style={{ width: `${Math.max(8, clickPercent)}%` }}
                          />
                        </div>
                      </div>
                    </TableCell>

                    {/* Status Badges */}
                    <TableCell>
                      <div className="flex flex-wrap items-center gap-1.5">
                        {link.passwordProtected ? (
                          <Badge variant="warning" className="text-[10px] gap-1">
                            <Lock className="w-2.5 h-2.5" />
                            {t.statusProtected}
                          </Badge>
                        ) : (
                          <Badge variant="success" className="text-[10px] gap-1">
                            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
                            {t.statusActive}
                          </Badge>
                        )}

                        {link.previewEnabled && (
                          <Badge variant="outline" className="text-[10px] text-emerald-800 bg-emerald-50/70 border-emerald-200">
                            <ShieldCheck className="w-2.5 h-2.5 mr-0.5 text-emerald-600" />
                            {t.statusPreviewEnabled}
                          </Badge>
                        )}

                        {(link.iosUrl || link.androidUrl || link.desktopUrl) && (
                          <Badge variant="outline" className="text-[10px] text-zinc-800 bg-zinc-100 border-zinc-200" title="Cihaza Göre Hedefleme Aktif">
                            <Smartphone className="w-2.5 h-2.5 mr-0.5 text-zinc-600" />
                            {lang === 'tr' ? 'Cihaz Hedefli' : 'Device Route'}
                          </Badge>
                        )}

                        {link.webhookUrl && (
                          <Badge variant="outline" className="text-[10px] text-purple-800 bg-purple-50/80 border-purple-200" title={`Webhook: ${link.webhookUrl}`}>
                            <Webhook className="w-2.5 h-2.5 mr-0.5 text-purple-600" />
                            <span>Webhook</span>
                          </Badge>
                        )}
                      </div>
                    </TableCell>

                    {/* Created Date */}
                    <TableCell className="text-zinc-500 font-mono text-[11px]">
                      {new Date(link.createdAt).toLocaleDateString(lang === 'tr' ? 'tr-TR' : 'en-US')}
                    </TableCell>

                    {/* Actions Toolbar */}
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-1">
                        {/* Security Preview Trigger */}
                        <Button
                          variant="ghost"
                          size="iconSm"
                          asChild
                          className="text-zinc-600 hover:text-zinc-950 hover:bg-zinc-100"
                          title={t.btnPreview}
                        >
                          <a href={`/preview/${link.shortCode}`} target="_blank" rel="noreferrer">
                            <Eye className="w-3.5 h-3.5" />
                          </a>
                        </Button>

                        {/* Password Unlock Modal Trigger */}
                        {link.passwordProtected && (
                          <Button
                            variant="ghost"
                            size="iconSm"
                            onClick={() => onOpenPasswordModal(link.shortCode)}
                            className="text-amber-700 hover:bg-amber-50"
                            title={t.btnVerifyPass}
                          >
                            <Lock className="w-3.5 h-3.5" />
                          </Button>
                        )}

                        {/* QR Code Modal Trigger */}
                        <Button
                          variant="ghost"
                          size="iconSm"
                          onClick={() => onOpenQr(link.shortCode)}
                          className="text-zinc-600 hover:text-zinc-950 hover:bg-zinc-100"
                          title={t.btnQr}
                        >
                          <QrCode className="w-3.5 h-3.5" />
                        </Button>

                        {/* Analytics Summary Modal Trigger */}
                        <Button
                          variant="ghost"
                          size="iconSm"
                          onClick={() => onOpenAnalyticsModal(link.shortCode)}
                          className="text-zinc-600 hover:text-zinc-950 hover:bg-zinc-100"
                          title={t.btnAnalytics}
                        >
                          <BarChart3 className="w-3.5 h-3.5" />
                        </Button>

                        {/* Delete Link */}
                        <Button
                          variant="ghost"
                          size="iconSm"
                          onClick={() => onDeleteLink(link.shortCode)}
                          className="text-zinc-400 hover:text-red-600 hover:bg-red-50"
                          title={t.btnDelete}
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                );
              })
            )}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  );
};

