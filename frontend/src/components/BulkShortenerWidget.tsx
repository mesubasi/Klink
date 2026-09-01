'use client';

import React, { useState } from 'react';
import { 
  Layers, 
  ArrowRight, 
  Check, 
  Copy, 
  ListOrdered, 
  ShieldCheck, 
  Eye, 
  Sparkles,
  Download,
  Trash2
} from 'lucide-react';
import { Language, translations } from '@/lib/translations';
import { BulkShortenRequest, BulkShortenResponse } from '@/lib/types';
import { ApiClient } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';

interface BulkShortenerWidgetProps {
  lang: Language;
  authUser: { u: string; p: string };
  onSuccessBatch: (response: BulkShortenResponse) => void;
}

export const BulkShortenerWidget: React.FC<BulkShortenerWidgetProps> = ({
  lang,
  authUser,
  onSuccessBatch,
}) => {
  const t = translations[lang];

  const [rawText, setRawText] = useState('');
  const [previewEnabled, setPreviewEnabled] = useState(false);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<BulkShortenResponse | null>(null);
  const [copiedIndex, setCopiedIndex] = useState<number | null>(null);
  const [allCopied, setAllCopied] = useState(false);

  const urlCount = rawText
    .split('\n')
    .map((line) => line.trim())
    .filter((line) => line.length > 0).length;

  const handleBulkSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const lines = rawText
      .split('\n')
      .map((line) => line.trim())
      .filter((line) => line.length > 0);

    if (lines.length === 0) return;

    setLoading(true);
    setResult(null);

    const req: BulkShortenRequest = {
      urls: lines.map((url) => ({ originalUrl: url, previewEnabled })),
    };

    try {
      const res = await ApiClient.bulkShortenUrls(req, lang, authUser);
      setResult(res);
      onSuccessBatch(res);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleLoadSample = () => {
    setRawText(
      `https://github.com/spring-projects/spring-boot\nhttps://nextjs.org/docs\nhttps://redis.io/documentation\nhttps://rabbitmq.com/tutorials`
    );
  };

  const handleCopySingle = (shortUrl: string, index: number) => {
    navigator.clipboard.writeText(shortUrl);
    setCopiedIndex(index);
    setTimeout(() => setCopiedIndex(null), 2000);
  };

  const handleCopyAll = () => {
    if (!result) return;
    const text = result.shortenedUrls.map((u) => `${u.shortUrl} -> ${u.originalUrl}`).join('\n');
    navigator.clipboard.writeText(text);
    setAllCopied(true);
    setTimeout(() => setAllCopied(false), 2000);
  };

  const handleDownloadCsv = () => {
    if (!result) return;
    const csvContent =
      'ShortCode,ShortUrl,OriginalUrl\n' +
      result.shortenedUrls
        .map((u) => `"${u.shortCode}","${u.shortUrl}","${u.originalUrl}"`)
        .join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `swiftlink-bulk-${Date.now()}.csv`;
    link.click();
  };

  return (
    <Card className="border-zinc-200/90 shadow-sm p-5 sm:p-7 space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 pb-4 border-b border-zinc-100">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-zinc-950 text-white flex items-center justify-center">
            <Layers className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-base font-bold text-zinc-950 tracking-tight">{t.bulkTitle}</h3>
              <Badge variant="secondary" className="font-mono text-[10px]">
                50 URL / Batch
              </Badge>
            </div>
            <p className="text-xs text-zinc-500 mt-0.5">{t.bulkDesc}</p>
          </div>
        </div>

        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={handleLoadSample}
          className="text-xs text-zinc-600 self-end sm:self-auto"
        >
          <Sparkles className="w-3.5 h-3.5 mr-1 text-zinc-500" />
          <span>{lang === 'tr' ? 'Örnek Yükle' : 'Load Sample'}</span>
        </Button>
      </div>

      <form onSubmit={handleBulkSubmit} className="space-y-4">
        <div className="space-y-1.5">
          <div className="flex items-center justify-between">
            <label className="text-xs font-semibold text-zinc-700 flex items-center gap-1.5">
              <ListOrdered className="w-3.5 h-3.5 text-zinc-400" />
              <span>{lang === 'tr' ? 'Bağlantı Listesi (Her satıra 1 URL)' : 'URL List (1 per line)'}</span>
            </label>
            <span className="text-[11px] font-mono font-semibold text-zinc-500">
              {urlCount} {lang === 'tr' ? 'link algılandı' : 'URLs detected'}
            </span>
          </div>

          <textarea
            rows={6}
            value={rawText}
            onChange={(e) => setRawText(e.target.value)}
            placeholder={t.bulkPlaceholder}
            className="w-full p-4 rounded-2xl border border-zinc-200/90 bg-zinc-50/50 font-mono text-xs text-zinc-900 leading-relaxed placeholder:text-zinc-400 focus:outline-none focus:border-zinc-900 focus:bg-white focus:ring-2 focus:ring-zinc-900/10 transition-all"
          />
        </div>

        {/* Security Preview Toggle Option */}
        <label className="flex items-start gap-3 cursor-pointer select-none py-1">
          <input
            type="checkbox"
            checked={previewEnabled}
            onChange={(e) => setPreviewEnabled(e.target.checked)}
            className="w-4 h-4 mt-0.5 rounded text-zinc-900 border-zinc-300 focus:ring-zinc-900 cursor-pointer"
          />
          <div>
            <span className="text-xs font-bold text-zinc-900 flex items-center gap-1.5">
              <ShieldCheck className="w-3.5 h-3.5 text-emerald-600" />
              {t.previewToggleOpt}
            </span>
            <p className="text-[11px] text-zinc-500 leading-normal">{t.previewToggleDesc}</p>
          </div>
        </label>

        <div className="flex items-center gap-2 pt-2">
          <Button
            type="submit"
            disabled={loading || urlCount === 0}
            className="w-full sm:w-auto bg-zinc-950 hover:bg-zinc-800 text-white font-semibold px-6 h-10"
          >
            {loading ? (
              <span>{lang === 'tr' ? 'Toplu İşleniyor...' : 'Processing...'}</span>
            ) : (
              <>
                <span>{t.btnBulkShorten} ({urlCount})</span>
                <ArrowRight className="w-4 h-4 ml-1.5" />
              </>
            )}
          </Button>

          {rawText && (
            <Button
              type="button"
              variant="ghost"
              size="sm"
              onClick={() => setRawText('')}
              className="text-zinc-500 hover:text-red-600"
            >
              <Trash2 className="w-3.5 h-3.5 mr-1" />
              <span>{lang === 'tr' ? 'Temizle' : 'Clear'}</span>
            </Button>
          )}
        </div>
      </form>

      {/* Results Container */}
      {result && (
        <div className="mt-6 space-y-3 pt-6 border-t border-zinc-100 animate-fadeIn">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2">
            <div>
              <h4 className="text-xs font-bold text-zinc-950 flex items-center gap-2">
                <span>{t.bulkResults}</span>
                <Badge variant="success" className="font-mono text-[10px]">
                  {result.successCount} / {result.totalCount} Başarılı
                </Badge>
              </h4>
            </div>

            <div className="flex items-center gap-2 self-end sm:self-auto">
              <Button
                variant="outline"
                size="sm"
                onClick={handleCopyAll}
                className="text-xs h-8"
              >
                {allCopied ? <Check className="w-3.5 h-3.5 text-emerald-600 mr-1" /> : <Copy className="w-3.5 h-3.5 mr-1" />}
                <span>{allCopied ? t.msgCopied : (lang === 'tr' ? 'Tümünü Kopyala' : 'Copy All')}</span>
              </Button>

              <Button
                variant="outline"
                size="sm"
                onClick={handleDownloadCsv}
                className="text-xs h-8"
              >
                <Download className="w-3.5 h-3.5 mr-1 text-emerald-600" />
                <span>CSV</span>
              </Button>
            </div>
          </div>

          <div className="space-y-2 max-h-72 overflow-y-auto pr-1">
            {result.shortenedUrls.map((item, idx) => (
              <div
                key={idx}
                className="p-3.5 rounded-xl bg-zinc-50 border border-zinc-200/80 flex items-center justify-between gap-3 text-xs hover:border-zinc-300 transition-colors"
              >
                <div className="overflow-hidden min-w-0 flex-1">
                  <div className="flex items-center gap-2">
                    <span className="font-bold text-zinc-950 font-mono block text-xs">{item.shortUrl}</span>
                    {item.previewEnabled && (
                      <span className="inline-flex items-center px-1.5 py-0.2 rounded text-[10px] font-semibold bg-emerald-50 text-emerald-800 border border-emerald-200">
                        <ShieldCheck className="w-2.5 h-2.5 mr-0.5" />
                        {t.statusPreviewEnabled}
                      </span>
                    )}
                  </div>
                  <p className="text-zinc-500 truncate text-[11px] font-mono mt-0.5">{item.originalUrl}</p>
                </div>

                <div className="flex items-center gap-1.5 shrink-0">
                  <Button
                    variant="outline"
                    size="sm"
                    asChild
                    className="h-8 text-xs bg-white"
                  >
                    <a href={`/preview/${item.shortCode}`} target="_blank" rel="noreferrer">
                      <Eye className="w-3.5 h-3.5 mr-1" />
                      <span>{t.btnPreview}</span>
                    </a>
                  </Button>

                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleCopySingle(item.shortUrl, idx)}
                    className="h-8 w-8 p-0 bg-white"
                  >
                    {copiedIndex === idx ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5 text-zinc-600" />}
                  </Button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </Card>
  );
};

