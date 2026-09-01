'use client';

import React, { useState, useEffect, useRef } from 'react';
import { 
  Download, 
  QrCode, 
  Copy, 
  Check, 
  Sparkles, 
  Palette, 
  Shapes, 
  Image as ImageIcon, 
  Upload, 
  Trash2, 
  AlertTriangle,
  CheckCircle2,
  Globe,
  Link2,
  Star,
  Shield,
  Coffee,
  Heart,
  Share2,
  Video,
  Send,
  MessageSquare
} from 'lucide-react';
import { Language, translations } from '@/lib/translations';
import { ApiClient } from '@/lib/api';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';

interface QrCodeModalProps {
  shortCode: string | null;
  lang: Language;
  onClose: () => void;
}

type DotStyle = 'square' | 'dots' | 'rounded';
type EyeStyle = 'square' | 'rounded' | 'pill';
type ActiveTab = 'colors' | 'shapes' | 'logo' | 'export';

interface ColorPreset {
  nameKey: keyof typeof translations.tr;
  fg: string;
  bg: string;
  eye: string;
}

const COLOR_PRESETS: ColorPreset[] = [
  { nameKey: 'qrPresetClassic', fg: '#09090b', bg: '#ffffff', eye: '#09090b' },
  { nameKey: 'qrPresetCyber', fg: '#8b5cf6', bg: '#0f172a', eye: '#06b6d4' },
  { nameKey: 'qrPresetEmerald', fg: '#059669', bg: '#f0fdf4', eye: '#047857' },
  { nameKey: 'qrPresetOcean', fg: '#0284c7', bg: '#f0f9ff', eye: '#2563eb' },
  { nameKey: 'qrPresetSunset', fg: '#e11d48', bg: '#fff1f2', eye: '#ea580c' },
  { nameKey: 'qrPresetLuxury', fg: '#d97706', bg: '#fffbeb', eye: '#b45309' },
];

export const QrCodeModal: React.FC<QrCodeModalProps> = ({ shortCode, lang, onClose }) => {
  const t = translations[lang];

  // Customization state
  const [activeTab, setActiveTab] = useState<ActiveTab>('colors');
  const [fgColor, setFgColor] = useState<string>('#09090b');
  const [bgColor, setBgColor] = useState<string>('#ffffff');
  const [eyeColor, setEyeColor] = useState<string>('#09090b');
  const [dotStyle, setDotStyle] = useState<DotStyle>('square');
  const [eyeStyle, setEyeStyle] = useState<EyeStyle>('square');
  const [selectedIcon, setSelectedIcon] = useState<string | null>(null);
  const [customLogoUrl, setCustomLogoUrl] = useState<string | null>(null);
  const [copied, setCopied] = useState<boolean>(false);
  const [isExporting, setIsExporting] = useState<boolean>(false);

  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const fullUrl = shortCode 
    ? (typeof window !== 'undefined' ? `${window.location.origin.replace('3000', '8080')}/${shortCode}` : `http://localhost:8080/${shortCode}`) 
    : 'https://klink.to';

  // Contrast calculation to ensure high scannability
  const calculateContrast = (hex1: string, hex2: string): number => {
    const getLuminance = (hex: string) => {
      const rgb = parseInt(hex.replace('#', ''), 16);
      const r = (rgb >> 16) & 0xff;
      const g = (rgb >> 8) & 0xff;
      const b = (rgb >> 0) & 0xff;
      return 0.299 * r + 0.587 * g + 0.114 * b;
    };
    try {
      const l1 = getLuminance(hex1);
      const l2 = getLuminance(hex2);
      return Math.abs(l1 - l2);
    } catch {
      return 255;
    }
  };

  const contrastScore = calculateContrast(fgColor, bgColor);
  const isContrastGood = contrastScore > 75;

  // Render QR Code onto the Canvas
  useEffect(() => {
    if (!shortCode || !canvasRef.current) return;

    const canvas = canvasRef.current;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const size = 512;
    canvas.width = size;
    canvas.height = size;

    // Load QR PNG image from backend with dynamic styling parameters
    const qrImg = new Image();
    qrImg.crossOrigin = 'anonymous';
    const qrSrc = ApiClient.getQrCodeUrl(shortCode, size, size, fgColor, bgColor, eyeColor, dotStyle, 'png');

    qrImg.onload = () => {
      ctx.clearRect(0, 0, size, size);
      ctx.drawImage(qrImg, 0, 0, size, size);

      // Render Center Logo if chosen
      const activeLogo = customLogoUrl || (selectedIcon ? getIconDataUrl(selectedIcon) : null);
      if (activeLogo) {
        const logoImg = new Image();
        logoImg.crossOrigin = 'anonymous';
        logoImg.onload = () => {
          const logoBoxSize = size * 0.22;
          const logoX = (size - logoBoxSize) / 2;
          const logoY = (size - logoBoxSize) / 2;
          const padding = logoBoxSize * 0.15;
          const badgeSize = logoBoxSize + padding * 2;
          const badgeX = (size - badgeSize) / 2;
          const badgeY = (size - badgeSize) / 2;
          const radius = badgeSize * 0.35;

          // Draw white badge background
          ctx.save();
          ctx.fillStyle = '#ffffff';
          ctx.beginPath();
          ctx.roundRect(badgeX, badgeY, badgeSize, badgeSize, radius);
          ctx.fill();

          // Border for badge
          ctx.strokeStyle = '#e4e4e7';
          ctx.lineWidth = 3;
          ctx.stroke();

          // Draw Logo inside badge
          ctx.drawImage(logoImg, logoX, logoY, logoBoxSize, logoBoxSize);
          ctx.restore();
        };
        logoImg.src = activeLogo;
      }
    };

    qrImg.src = qrSrc;
  }, [shortCode, fgColor, bgColor, eyeColor, dotStyle, eyeStyle, selectedIcon, customLogoUrl]);

  // Helper to generate SVG icon data URLs for presets
  const getIconDataUrl = (iconName: string): string => {
    let svgString = '';
    const color = fgColor === '#ffffff' ? '#09090b' : fgColor;
    
    switch (iconName) {
      case 'github':
        svgString = `<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M15 22v-4a4.8 4.8 0 0 0-1-3.5c3 0 6-2 6-5.5.08-1.25-.27-2.48-1-3.5.28-1.15.28-2.35 0-3.5 0 0-1 0-3 1.5-2.64-.5-5.36-.5-8 0C6 2 5 2 5 2c-.3 1.15-.3 2.35 0 3.5A5.403 5.403 0 0 0 4 9c0 3.5 3 5.5 6 5.5-.39.49-.68 1.05-.85 1.65-.17.6-.22 1.23-.15 1.85v4"/><path d="M9 18c-4.51 2-5-2-7-2"/></svg>`;
        break;
      case 'twitter':
        svgString = `<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 4s-.7 2.1-2 3.4c1.6 10-9.4 17.3-18 11.6 2.2.1 4.4-.6 6-2C3 15.5.5 9.6 3 5c2.2 2.6 5.6 4.1 9 4-.9-4.2 4-6.6 7-3.8 1.1 0 3-1.2 3-1.2z"/></svg>`;
        break;
      case 'instagram':
        svgString = `<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="20" height="20" x="2" y="2" rx="5" ry="5"/><path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"/><line x1="17.5" x2="17.51" y1="6.5" y2="6.5"/></svg>`;
        break;
      case 'youtube':
        svgString = `<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2.5 17a24.12 24.12 0 0 1 0-10 2 2 0 0 1 1.4-1.4 49.56 49.56 0 0 1 16.2 0A2 2 0 0 1 21.5 7a24.12 24.12 0 0 1 0 10 2 2 0 0 1-1.4 1.4 49.55 49.55 0 0 1-16.2 0A2 2 0 0 1 2.5 17"/><polygon points="10 15 15 12 10 9 10 15"/></svg>`;
        break;
      case 'linkedin':
        svgString = `<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 8a6 6 0 0 1 6 6v7h-4v-7a2 2 0 0 0-2-2 2 2 0 0 0-2 2v7h-4v-7a6 6 0 0 1 6-6z"/><rect width="4" height="12" x="2" y="9"/><circle cx="4" cy="4" r="2"/></svg>`;
        break;
      case 'shield':
        svgString = `<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z"/></svg>`;
        break;
      case 'coffee':
        svgString = `<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10 2v2"/><path d="M14 2v2"/><path d="M16 8a1 1 0 0 1 1 1v8a4 4 0 0 1-4 4H7a4 4 0 0 1-4-4V9a1 1 0 0 1 1-1h12Z"/><path d="M6 2v2"/><path d="M17 12h3a2 2 0 0 1 2 2v1a2 2 0 0 1-2 2h-3"/></svg>`;
        break;
      case 'heart':
        svgString = `<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/></svg>`;
        break;
      default:
        svgString = `<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 2a14.5 14.5 0 0 0 0 20 14.5 14.5 0 0 0 0-20"/><path d="M2 12h20"/></svg>`;
    }
    return `data:image/svg+xml;utf8,${encodeURIComponent(svgString)}`;
  };

  // Handle custom logo file upload
  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      if (event.target?.result) {
        setSelectedIcon(null);
        setCustomLogoUrl(event.target.result as string);
      }
    };
    reader.readAsDataURL(file);
  };

  // Download high-res PNG
  const handleDownloadPng = (downloadSize: number = 512) => {
    if (!canvasRef.current || !shortCode) return;
    setIsExporting(true);

    try {
      const exportCanvas = document.createElement('canvas');
      exportCanvas.width = downloadSize;
      exportCanvas.height = downloadSize;
      const exportCtx = exportCanvas.getContext('2d');

      if (exportCtx && canvasRef.current) {
        exportCtx.drawImage(canvasRef.current, 0, 0, downloadSize, downloadSize);
        const dataUrl = exportCanvas.toDataURL('image/png');
        const a = document.createElement('a');
        a.href = dataUrl;
        a.download = `klink-qr-${shortCode}-${downloadSize}px.png`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
      }
    } finally {
      setIsExporting(false);
    }
  };

  // Download Vector SVG
  const handleDownloadSvg = () => {
    if (!shortCode) return;
    const svgUrl = ApiClient.getQrCodeUrl(shortCode, 512, 512, fgColor, bgColor, eyeColor, dotStyle, 'svg');
    const a = document.createElement('a');
    a.href = svgUrl;
    a.download = `klink-qr-${shortCode}.svg`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  };

  // Copy Canvas Image to Clipboard
  const handleCopyToClipboard = async () => {
    if (!canvasRef.current) return;
    try {
      canvasRef.current.toBlob(async (blob) => {
        if (!blob) return;
        await navigator.clipboard.write([
          new ClipboardItem({ 'image/png': blob })
        ]);
        setCopied(true);
        setTimeout(() => setCopied(false), 2500);
      });
    } catch (err) {
      console.error('Pano kopyalama hatası: ', err);
    }
  };

  return (
    <Dialog open={!!shortCode} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-3xl p-0 overflow-hidden border-zinc-200/90 shadow-2xl bg-white rounded-3xl">
        
        {/* Header */}
        <DialogHeader className="px-6 py-4 border-b border-zinc-100 bg-zinc-50/50 flex flex-row items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl bg-zinc-950 text-white flex items-center justify-center shadow-xs">
              <Sparkles className="w-5 h-5 text-emerald-400" />
            </div>
            <div>
              <DialogTitle className="text-base font-bold text-zinc-950 flex items-center gap-2">
                {t.qrStudioTitle}
                <span className="text-[10px] px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-700 font-semibold border border-emerald-200 uppercase tracking-wider">
                  Pro Studio
                </span>
              </DialogTitle>
              <DialogDescription className="text-xs text-zinc-500 font-mono">
                swift.link/{shortCode}
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>

        {/* Studio Layout */}
        <div className="grid grid-cols-1 md:grid-cols-12 gap-0">
          
          {/* Left Column: Live Canvas Preview & Quick Export */}
          <div className="md:col-span-5 p-6 bg-zinc-50/80 border-r border-zinc-200/70 flex flex-col items-center justify-between space-y-4">
            
            {/* Canvas Frame */}
            <div className="w-full flex flex-col items-center justify-center space-y-3">
              <div className="relative p-3 bg-white rounded-2xl border border-zinc-200 shadow-md flex items-center justify-center">
                <canvas 
                  ref={canvasRef} 
                  className="w-52 h-52 object-contain rounded-xl"
                />
              </div>

              {/* Scannability / Contrast Badge */}
              <div className={`flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium border ${
                isContrastGood 
                  ? 'bg-emerald-50 text-emerald-700 border-emerald-200' 
                  : 'bg-amber-50 text-amber-800 border-amber-200'
              }`}>
                {isContrastGood ? (
                  <>
                    <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                    <span>{t.qrContrastGood}</span>
                  </>
                ) : (
                  <>
                    <AlertTriangle className="w-3.5 h-3.5 text-amber-600" />
                    <span className="text-[11px] leading-tight">{t.qrContrastWarn}</span>
                  </>
                )}
              </div>
            </div>

            {/* Quick Export Actions */}
            <div className="w-full space-y-2 pt-2 border-t border-zinc-200/80">
              <Button 
                onClick={() => handleDownloadPng(512)} 
                disabled={isExporting}
                className="w-full bg-zinc-950 hover:bg-zinc-800 text-white font-semibold h-10 rounded-xl shadow-xs"
              >
                <Download className="w-4 h-4 mr-2" />
                <span>{t.qrExportPng}</span>
              </Button>

              <div className="grid grid-cols-2 gap-2">
                <Button 
                  onClick={handleDownloadSvg}
                  variant="outline" 
                  className="w-full text-xs h-9 rounded-xl border-zinc-200 hover:bg-zinc-100 font-medium"
                >
                  <Download className="w-3.5 h-3.5 mr-1 text-zinc-500" />
                  <span>{t.qrExportSvg}</span>
                </Button>

                <Button 
                  onClick={handleCopyToClipboard}
                  variant="outline" 
                  className="w-full text-xs h-9 rounded-xl border-zinc-200 hover:bg-zinc-100 font-medium"
                >
                  {copied ? (
                    <>
                      <Check className="w-3.5 h-3.5 mr-1 text-emerald-600" />
                      <span className="text-emerald-700 font-semibold">{t.qrCopied}</span>
                    </>
                  ) : (
                    <>
                      <Copy className="w-3.5 h-3.5 mr-1 text-zinc-500" />
                      <span>{t.qrCopyClipboard}</span>
                    </>
                  )}
                </Button>
              </div>
            </div>

          </div>

          {/* Right Column: Customization Tabs & Controls */}
          <div className="md:col-span-7 p-6 flex flex-col justify-between space-y-5">
            
            {/* Tabs Header */}
            <div className="flex border-b border-zinc-100 pb-2 gap-2">
              <button
                onClick={() => setActiveTab('colors')}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
                  activeTab === 'colors' 
                    ? 'bg-zinc-900 text-white shadow-xs' 
                    : 'text-zinc-600 hover:text-zinc-900 hover:bg-zinc-100'
                }`}
              >
                <Palette className="w-3.5 h-3.5" />
                <span>{t.qrTabColors}</span>
              </button>

              <button
                onClick={() => setActiveTab('shapes')}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
                  activeTab === 'shapes' 
                    ? 'bg-zinc-900 text-white shadow-xs' 
                    : 'text-zinc-600 hover:text-zinc-900 hover:bg-zinc-100'
                }`}
              >
                <Shapes className="w-3.5 h-3.5" />
                <span>{t.qrTabShapes}</span>
              </button>

              <button
                onClick={() => setActiveTab('logo')}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
                  activeTab === 'logo' 
                    ? 'bg-zinc-900 text-white shadow-xs' 
                    : 'text-zinc-600 hover:text-zinc-900 hover:bg-zinc-100'
                }`}
              >
                <ImageIcon className="w-3.5 h-3.5" />
                <span>{t.qrTabLogo}</span>
              </button>

              <button
                onClick={() => setActiveTab('export')}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
                  activeTab === 'export' 
                    ? 'bg-zinc-900 text-white shadow-xs' 
                    : 'text-zinc-600 hover:text-zinc-900 hover:bg-zinc-100'
                }`}
              >
                <Download className="w-3.5 h-3.5" />
                <span>{t.qrTabExport}</span>
              </button>
            </div>

            {/* Tab 1: Colors & Presets */}
            {activeTab === 'colors' && (
              <div className="space-y-4">
                {/* Presets */}
                <div>
                  <label className="text-xs font-bold text-zinc-700 mb-2 block">{t.qrPresetThemes}</label>
                  <div className="grid grid-cols-3 gap-2">
                    {COLOR_PRESETS.map((preset, idx) => (
                      <button
                        key={idx}
                        onClick={() => {
                          setFgColor(preset.fg);
                          setBgColor(preset.bg);
                          setEyeColor(preset.eye);
                        }}
                        className="flex items-center gap-2 p-2 rounded-xl border border-zinc-200 hover:border-zinc-400 bg-white hover:bg-zinc-50 transition-all text-left group"
                      >
                        <div className="flex -space-x-1 shrink-0">
                          <span className="w-4 h-4 rounded-full border border-white shadow-2xs" style={{ backgroundColor: preset.fg }} />
                          <span className="w-4 h-4 rounded-full border border-white shadow-2xs" style={{ backgroundColor: preset.bg }} />
                        </div>
                        <span className="text-[11px] font-medium text-zinc-700 truncate group-hover:text-zinc-950">
                          {t[preset.nameKey]}
                        </span>
                      </button>
                    ))}
                  </div>
                </div>

                {/* Color Pickers */}
                <div className="space-y-3 pt-2 border-t border-zinc-100">
                  <div className="grid grid-cols-3 gap-3">
                    {/* Foreground */}
                    <div>
                      <span className="text-[11px] font-semibold text-zinc-600 mb-1 block">{t.qrFgColor}</span>
                      <div className="flex items-center gap-2">
                        <input
                          type="color"
                          value={fgColor}
                          onChange={(e) => setFgColor(e.target.value)}
                          className="w-8 h-8 rounded-lg border border-zinc-200 cursor-pointer p-0 bg-transparent"
                        />
                        <span className="text-xs font-mono text-zinc-600 uppercase">{fgColor}</span>
                      </div>
                    </div>

                    {/* Background */}
                    <div>
                      <span className="text-[11px] font-semibold text-zinc-600 mb-1 block">{t.qrBgColor}</span>
                      <div className="flex items-center gap-2">
                        <input
                          type="color"
                          value={bgColor}
                          onChange={(e) => setBgColor(e.target.value)}
                          className="w-8 h-8 rounded-lg border border-zinc-200 cursor-pointer p-0 bg-transparent"
                        />
                        <span className="text-xs font-mono text-zinc-600 uppercase">{bgColor}</span>
                      </div>
                    </div>

                    {/* Eye Frame */}
                    <div>
                      <span className="text-[11px] font-semibold text-zinc-600 mb-1 block">{t.qrEyeColor}</span>
                      <div className="flex items-center gap-2">
                        <input
                          type="color"
                          value={eyeColor}
                          onChange={(e) => setEyeColor(e.target.value)}
                          className="w-8 h-8 rounded-lg border border-zinc-200 cursor-pointer p-0 bg-transparent"
                        />
                        <span className="text-xs font-mono text-zinc-600 uppercase">{eyeColor}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Tab 2: Shapes & Patterns */}
            {activeTab === 'shapes' && (
              <div className="space-y-4">
                {/* Dot Style */}
                <div>
                  <label className="text-xs font-bold text-zinc-700 mb-2 block">{t.qrTabShapes}</label>
                  <div className="grid grid-cols-3 gap-2">
                    <button
                      onClick={() => setDotStyle('square')}
                      className={`p-3 rounded-xl border flex flex-col items-center justify-center gap-2 text-center transition-all ${
                        dotStyle === 'square'
                          ? 'border-zinc-950 bg-zinc-950 text-white shadow-xs'
                          : 'border-zinc-200 bg-white hover:bg-zinc-50 text-zinc-700'
                      }`}
                    >
                      <div className="w-5 h-5 rounded-none bg-current" />
                      <span className="text-xs font-medium">{t.qrDotSquare}</span>
                    </button>

                    <button
                      onClick={() => setDotStyle('dots')}
                      className={`p-3 rounded-xl border flex flex-col items-center justify-center gap-2 text-center transition-all ${
                        dotStyle === 'dots'
                          ? 'border-zinc-950 bg-zinc-950 text-white shadow-xs'
                          : 'border-zinc-200 bg-white hover:bg-zinc-50 text-zinc-700'
                      }`}
                    >
                      <div className="w-5 h-5 rounded-full bg-current" />
                      <span className="text-xs font-medium">{t.qrDotDots}</span>
                    </button>

                    <button
                      onClick={() => setDotStyle('rounded')}
                      className={`p-3 rounded-xl border flex flex-col items-center justify-center gap-2 text-center transition-all ${
                        dotStyle === 'rounded'
                          ? 'border-zinc-950 bg-zinc-950 text-white shadow-xs'
                          : 'border-zinc-200 bg-white hover:bg-zinc-50 text-zinc-700'
                      }`}
                    >
                      <div className="w-5 h-5 rounded-md bg-current" />
                      <span className="text-xs font-medium">{t.qrDotRounded}</span>
                    </button>
                  </div>
                </div>

                {/* Eye Style */}
                <div className="pt-2 border-t border-zinc-100">
                  <label className="text-xs font-bold text-zinc-700 mb-2 block">Köşe Göz Çerçevesi</label>
                  <div className="grid grid-cols-3 gap-2">
                    <button
                      onClick={() => setEyeStyle('square')}
                      className={`p-2.5 rounded-xl border text-xs font-medium text-center transition-all ${
                        eyeStyle === 'square'
                          ? 'border-zinc-900 bg-zinc-100 text-zinc-950 font-bold'
                          : 'border-zinc-200 bg-white hover:bg-zinc-50 text-zinc-600'
                      }`}
                    >
                      {t.qrEyeSquare}
                    </button>
                    <button
                      onClick={() => setEyeStyle('rounded')}
                      className={`p-2.5 rounded-xl border text-xs font-medium text-center transition-all ${
                        eyeStyle === 'rounded'
                          ? 'border-zinc-900 bg-zinc-100 text-zinc-950 font-bold'
                          : 'border-zinc-200 bg-white hover:bg-zinc-50 text-zinc-600'
                      }`}
                    >
                      {t.qrEyeRounded}
                    </button>
                    <button
                      onClick={() => setEyeStyle('pill')}
                      className={`p-2.5 rounded-xl border text-xs font-medium text-center transition-all ${
                        eyeStyle === 'pill'
                          ? 'border-zinc-900 bg-zinc-100 text-zinc-950 font-bold'
                          : 'border-zinc-200 bg-white hover:bg-zinc-50 text-zinc-600'
                      }`}
                    >
                      {t.qrEyePill}
                    </button>
                  </div>
                </div>
              </div>
            )}

            {/* Tab 3: Logo & Icons */}
            {activeTab === 'logo' && (
              <div className="space-y-4">
                {/* Social & Brand Icons Grid */}
                <div>
                  <label className="text-xs font-bold text-zinc-700 mb-2 block">{t.qrLogoPick}</label>
                  <div className="grid grid-cols-5 gap-2">
                    {[
                      { id: 'globe', icon: Globe, label: 'Web' },
                      { id: 'link', icon: Link2, label: 'Link' },
                      { id: 'star', icon: Star, label: 'Star' },
                      { id: 'send', icon: Send, label: 'Send' },
                      { id: 'video', icon: Video, label: 'Video' },
                      { id: 'message', icon: MessageSquare, label: 'Chat' },
                      { id: 'shield', icon: Shield, label: 'Shield' },
                      { id: 'coffee', icon: Coffee, label: 'Coffee' },
                      { id: 'heart', icon: Heart, label: 'Heart' },
                      { id: 'share', icon: Share2, label: 'Share' },
                    ].map((item) => {
                      const IconComp = item.icon;
                      const isSelected = selectedIcon === item.id && !customLogoUrl;
                      return (
                        <button
                          key={item.id}
                          onClick={() => {
                            setCustomLogoUrl(null);
                            setSelectedIcon(isSelected ? null : item.id);
                          }}
                          className={`p-2 rounded-xl border flex flex-col items-center justify-center gap-1 transition-all ${
                            isSelected
                              ? 'border-zinc-950 bg-zinc-950 text-white shadow-xs'
                              : 'border-zinc-200 bg-white hover:bg-zinc-50 text-zinc-700'
                          }`}
                        >
                          <IconComp className="w-4 h-4" />
                          <span className="text-[10px] font-medium truncate w-full text-center">{item.label}</span>
                        </button>
                      );
                    })}
                  </div>
                </div>

                {/* Custom Logo Upload */}
                <div className="pt-2 border-t border-zinc-100">
                  <label className="text-xs font-bold text-zinc-700 mb-1.5 block">{t.qrLogoUpload}</label>
                  <div className="flex items-center gap-3">
                    <input
                      type="file"
                      ref={fileInputRef}
                      onChange={handleFileUpload}
                      accept="image/png,image/jpeg,image/svg+xml"
                      className="hidden"
                    />
                    <Button
                      onClick={() => fileInputRef.current?.click()}
                      variant="outline"
                      className="w-full text-xs h-9 rounded-xl border-dashed border-zinc-300 hover:border-zinc-400 hover:bg-zinc-50"
                    >
                      <Upload className="w-3.5 h-3.5 mr-1.5 text-zinc-500" />
                      <span>{t.qrLogoUpload}</span>
                    </Button>

                    {(selectedIcon || customLogoUrl) && (
                      <Button
                        onClick={() => {
                          setSelectedIcon(null);
                          setCustomLogoUrl(null);
                        }}
                        variant="destructive"
                        className="text-xs h-9 px-3 rounded-xl shrink-0"
                      >
                        <Trash2 className="w-3.5 h-3.5 mr-1" />
                        <span>{t.qrLogoRemove}</span>
                      </Button>
                    )}
                  </div>
                </div>
              </div>
            )}

            {/* Tab 4: Export & Formats */}
            {activeTab === 'export' && (
              <div className="space-y-4">
                <div className="p-3.5 rounded-2xl bg-zinc-50 border border-zinc-200/80 space-y-2">
                  <h4 className="text-xs font-bold text-zinc-900">Yüksek Çözünürlüklü Dışa Aktarma</h4>
                  <p className="text-[11px] text-zinc-500 leading-relaxed">
                    Baskı, kartvizit, afiş veya dijital paylaşımlar için kayıpsız vektörel veya 4K Ultra HD formatları tercih edebilirsiniz.
                  </p>
                </div>

                <div className="space-y-2">
                  <Button 
                    onClick={() => handleDownloadPng(512)} 
                    variant="outline"
                    className="w-full justify-between h-10 rounded-xl text-xs font-medium border-zinc-200 hover:bg-zinc-50"
                  >
                    <span className="flex items-center gap-2">
                      <Download className="w-4 h-4 text-zinc-500" />
                      <span>Standart Web PNG (512x512)</span>
                    </span>
                    <span className="text-zinc-400 font-mono text-[10px]">~120 KB</span>
                  </Button>

                  <Button 
                    onClick={() => handleDownloadPng(2048)} 
                    className="w-full justify-between h-10 rounded-xl text-xs font-semibold bg-zinc-900 hover:bg-zinc-800 text-white"
                  >
                    <span className="flex items-center gap-2">
                      <Sparkles className="w-4 h-4 text-emerald-400" />
                      <span>Ultra HD Baskı PNG (2048x2048)</span>
                    </span>
                    <span className="text-zinc-300 font-mono text-[10px]">4K Crisp</span>
                  </Button>

                  <Button 
                    onClick={handleDownloadSvg} 
                    variant="outline"
                    className="w-full justify-between h-10 rounded-xl text-xs font-medium border-zinc-200 hover:bg-zinc-50"
                  >
                    <span className="flex items-center gap-2">
                      <QrCode className="w-4 h-4 text-indigo-500" />
                      <span>Vektörel SVG (Kayıpsız Ölçeklenebilir)</span>
                    </span>
                    <span className="text-indigo-600 font-semibold text-[10px]">Vector</span>
                  </Button>
                </div>
              </div>
            )}

            {/* Footer target link info */}
            <div className="pt-3 border-t border-zinc-100 flex items-center justify-between text-[11px] text-zinc-400">
              <span className="font-mono truncate max-w-[240px]">
                Hedef: {fullUrl}
              </span>
              <span className="font-mono">
                Error Correction Level: H (%30)
              </span>
            </div>

          </div>

        </div>

      </DialogContent>
    </Dialog>
  );
};
