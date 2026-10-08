'use client';

import React, { useCallback, useEffect, useState } from 'react';
import { AuditEventResponse, AuditQuery, PagedResponse } from '@/lib/types';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';

const ACTION_LABELS: Record<string, string> = {
  USER_REGISTERED: 'Hesap oluşturuldu',
  LOGIN_SUCCESS: 'Giriş yapıldı',
  LOGIN_FAILED: 'Başarısız giriş',
  LOGOUT_EVERYWHERE: 'Tüm oturumlar kapatıldı',
  PASSWORD_RESET_REQUESTED: 'Parola sıfırlama istendi',
  PASSWORD_RESET_COMPLETED: 'Parola sıfırlandı',
  EMAIL_VERIFIED: 'E-posta doğrulandı',
  TWO_FACTOR_ENABLED: '2FA açıldı',
  TWO_FACTOR_DISABLED: '2FA kapatıldı',
  ADMIN_USER_ROLE_CHANGED: 'Kullanıcı rolü değiştirildi',
  ADMIN_USER_DELETED: 'Kullanıcı silindi',
  ADMIN_API_KEY_DECISION: 'API anahtarı kararı',
  CUSTOMER_CREATED: 'Müşteri oluşturuldu',
  QUOTA_UPDATED: 'Kota güncellendi',
  ADMIN_VIEWED_WORKSPACE: 'Platform yöneticisi çalışma alanını görüntüledi',
  ADMIN_VIEWED_WORKSPACE_LINKS: 'Platform yöneticisi linkleri görüntüledi',
  ADMIN_VIEWED_PERMISSIONS: 'Platform yöneticisi izinleri görüntüledi',
  ADMIN_ACCESSED_LINK: 'Platform yöneticisi link verisine erişti',
  WORKSPACE_CREATED: 'Çalışma alanı oluşturuldu',
  MEMBER_ADDED: 'Üye eklendi',
  MEMBER_ROLE_CHANGED: 'Üye rolü değişti',
  MEMBER_REMOVED: 'Üye çıkarıldı',
  INVITATION_SENT: 'Davet gönderildi',
  INVITATION_REVOKED: 'Davet iptal edildi',
  INVITATION_ACCEPTED: 'Davet kabul edildi',
  PERMISSIONS_UPDATED: 'İzin matrisi güncellendi',
  LINK_DELETED: 'Link silindi',
  LINK_STATUS_CHANGED: 'Link durumu değişti',
  LINK_REPORT_EXPORTED: 'Rapor dışa aktarıldı',
  ACCESS_DENIED: 'Erişim reddedildi',
};

interface AuditLogTableProps {
  /** Loads one page; the table owns filters and paging. */
  load: (query: AuditQuery) => Promise<PagedResponse<AuditEventResponse>>;
  /** Action codes offered in the filter; omit to hide the filter. */
  actions?: string[];
  /** Changing this reloads the table (e.g. the selected workspace). */
  reloadKey?: string;
  dark?: boolean;
}

export function AuditLogTable({ load, actions, reloadKey, dark = false }: AuditLogTableProps) {
  const [actor, setActor] = useState('');
  const [action, setAction] = useState('');
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PagedResponse<AuditEventResponse> | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const fetchPage = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      setData(await load({ actor: actor.trim() || undefined, action: action || undefined, page, size: 25 }));
    } catch (e: any) {
      setError(e.message || 'Denetim kaydı alınamadı.');
    } finally {
      setLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [actor, action, page, reloadKey]);

  useEffect(() => {
    const timer = setTimeout(fetchPage, 250);
    return () => clearTimeout(timer);
  }, [fetchPage]);

  const input = dark
    ? 'bg-zinc-800 border border-zinc-700 text-white placeholder:text-zinc-500'
    : 'bg-white border border-zinc-200 text-zinc-900';
  const muted = dark ? 'text-zinc-400' : 'text-zinc-500';
  const head = dark ? 'text-zinc-400 text-xs' : 'text-xs';
  const rowClass = dark ? 'border-zinc-800/60 hover:bg-zinc-800/30' : '';

  const outcomeBadge = (e: AuditEventResponse) => {
    if (e.outcome === 'DENIED') return <Badge variant="destructive" className="text-[10px]">REDDEDİLDİ</Badge>;
    if (e.outcome === 'FAILURE') return <Badge variant="destructive" className="text-[10px]">BAŞARISIZ</Badge>;
    return <Badge variant="secondary" className="text-[10px]">TAMAM</Badge>;
  };

  return (
    <div>
      <div className="flex flex-wrap items-center gap-2 p-4">
        <input
          value={actor}
          onChange={(e) => { setActor(e.target.value); setPage(0); }}
          placeholder="Kullanıcı adı"
          className={`rounded-lg px-3 py-2 text-xs ${input}`}
        />
        {actions && (
          <select
            value={action}
            onChange={(e) => { setAction(e.target.value); setPage(0); }}
            className={`rounded-lg px-3 py-2 text-xs ${input}`}
          >
            <option value="">Tüm işlemler</option>
            {actions.map((a) => (
              <option key={a} value={a}>{ACTION_LABELS[a] || a}</option>
            ))}
          </select>
        )}
        <span className={`text-[11px] ${muted}`}>
          {data ? `${data.totalElements} kayıt` : ''} {loading ? '· yükleniyor...' : ''}
        </span>
      </div>

      {error && <div className="px-4 pb-3 text-xs text-red-500">{error}</div>}

      <Table>
        <TableHeader>
          <TableRow className={dark ? 'border-zinc-800 hover:bg-transparent' : ''}>
            <TableHead className={head}>Zaman</TableHead>
            <TableHead className={head}>Kim</TableHead>
            <TableHead className={head}>İşlem</TableHead>
            <TableHead className={head}>Hedef</TableHead>
            <TableHead className={head}>Ayrıntı</TableHead>
            <TableHead className={head}>Sonuç</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {data && data.content.length === 0 && (
            <TableRow>
              <TableCell colSpan={6} className={`h-20 text-center text-xs ${muted}`}>Kayıt bulunamadı.</TableCell>
            </TableRow>
          )}
          {data?.content.map((e) => (
            <TableRow key={e.id} className={rowClass}>
              <TableCell className={`text-xs whitespace-nowrap ${muted}`}>{new Date(e.occurredAt).toLocaleString('tr-TR')}</TableCell>
              <TableCell className="text-xs">
                {e.actorUsername ? <span className="font-medium">@{e.actorUsername}</span> : <span className={muted}>—</span>}
                {e.actorRole === 'ROLE_ADMIN' && <Badge variant="outline" className="ml-1 text-[9px]">PLATFORM</Badge>}
                {e.ip && <div className={`text-[10px] font-mono ${muted}`}>{e.ip}</div>}
              </TableCell>
              <TableCell className="text-xs">{ACTION_LABELS[e.action] || e.action}</TableCell>
              <TableCell className={`text-xs ${muted}`}>
                {e.targetType ? `${e.targetType}: ` : ''}<span className="font-mono">{e.targetId || '—'}</span>
              </TableCell>
              <TableCell className={`text-xs max-w-xs break-words ${muted}`}>{e.details || ''}</TableCell>
              <TableCell>{outcomeBadge(e)}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>

      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-between p-4">
          <span className={`text-xs ${muted}`}>Sayfa {data.page + 1} / {data.totalPages}</span>
          <div className="flex gap-2">
            <Button variant="outline" size="sm" disabled={loading || page === 0} onClick={() => setPage(page - 1)}>Önceki</Button>
            <Button variant="outline" size="sm" disabled={loading || page >= data.totalPages - 1} onClick={() => setPage(page + 1)}>Sonraki</Button>
          </div>
        </div>
      )}
    </div>
  );
}
