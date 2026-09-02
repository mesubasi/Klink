'use client';

import React, { useState, useEffect } from 'react';
import { 
  Building2, 
  Users, 
  Plus, 
  Shield, 
  ShieldAlert, 
  UserPlus, 
  Trash2, 
  Crown, 
  Eye, 
  UserCheck, 
  Link as LinkIcon, 
  ExternalLink,
  CheckCircle2,
  AlertCircle,
  Clock,
  Layers,
  Save,
  Lock,
  Zap,
  Check,
  X
} from 'lucide-react';
import { 
  WorkspaceResponse, 
  WorkspaceMemberResponse, 
  WorkspaceRole, 
  ShortenResponse,
  WorkspacePermissionMatrixResponse,
  RolePermissionDto 
} from '@/lib/types';
import { ApiClient } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '@/components/ui/dialog';

interface WorkspaceManagerWidgetProps {
  authUser: { u: string; p: string; token?: string; role?: string } | null;
  onSelectWorkspaceForLinks?: (workspace: WorkspaceResponse | null) => void;
}

export function WorkspaceManagerWidget({ authUser, onSelectWorkspaceForLinks }: WorkspaceManagerWidgetProps) {
  const [workspaces, setWorkspaces] = useState<WorkspaceResponse[]>([]);
  const [selectedWorkspace, setSelectedWorkspace] = useState<WorkspaceResponse | null>(null);
  const [workspaceUrls, setWorkspaceUrls] = useState<ShortenResponse[]>([]);
  const [permissionMatrix, setPermissionMatrix] = useState<WorkspacePermissionMatrixResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [savingMatrix, setSavingMatrix] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // Modals
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);
  const [newWsName, setNewWsName] = useState('');
  const [newWsDesc, setNewWsDesc] = useState('');
  const [inviteEmail, setInviteEmail] = useState('');
  const [inviteRole, setInviteRole] = useState<WorkspaceRole>('MEMBER');

  const [activeTab, setActiveTab] = useState<'members' | 'urls' | 'matrix'>('members');

  useEffect(() => {
    if (authUser) {
      loadWorkspaces();
    }
  }, [authUser]);

  const loadWorkspaces = async () => {
    if (!authUser) return;
    setLoading(true);
    setErrorMsg(null);
    try {
      const list = await ApiClient.getUserWorkspaces('tr', authUser);
      setWorkspaces(list);
      if (list.length > 0 && !selectedWorkspace) {
        selectWorkspace(list[0].id);
      }
    } catch (err: any) {
      setErrorMsg(err.message || 'Çalışma alanları yüklenirken hata oluştu.');
    } finally {
      setLoading(false);
    }
  };

  const selectWorkspace = async (wsId: string) => {
    if (!authUser) return;
    setErrorMsg(null);
    try {
      const details = await ApiClient.getWorkspaceDetails(wsId, 'tr', authUser);
      setSelectedWorkspace(details);
      if (onSelectWorkspaceForLinks) {
        onSelectWorkspaceForLinks(details);
      }
      // Also fetch URLs and Permission Matrix
      const urls = await ApiClient.getWorkspaceUrls(wsId, 'tr', authUser);
      setWorkspaceUrls(urls);

      const matrix = await ApiClient.getWorkspacePermissionMatrix(wsId, 'tr', authUser);
      setPermissionMatrix(matrix);
    } catch (err: any) {
      setErrorMsg(err.message || 'Çalışma alanı detayları alınamadı.');
    }
  };

  const handleToggleMatrixPerm = (role: 'member' | 'viewer', permKey: keyof RolePermissionDto) => {
    if (!permissionMatrix || selectedWorkspace?.currentUserRole !== 'ADMIN') return;
    setPermissionMatrix({
      ...permissionMatrix,
      [role]: {
        ...permissionMatrix[role],
        [permKey]: !permissionMatrix[role][permKey],
      },
    });
  };

  const handleSaveMatrix = async () => {
    if (!authUser || !selectedWorkspace || !permissionMatrix) return;
    setSavingMatrix(true);
    setErrorMsg(null);
    try {
      const updated = await ApiClient.updateWorkspacePermissionMatrix(selectedWorkspace.id, {
        member: permissionMatrix.member,
        viewer: permissionMatrix.viewer,
      }, 'tr', authUser);
      setPermissionMatrix(updated);
      setSuccessMsg('İzin matrisi başarıyla güncellendi ve Redis önbelleği anında yenilendi.');
    } catch (err: any) {
      setErrorMsg(err.message || 'İzin matrisi güncellenirken hata oluştu.');
    } finally {
      setSavingMatrix(false);
    }
  };

  const handleCreateWorkspace = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!authUser || !newWsName.trim()) return;
    setErrorMsg(null);
    try {
      const created = await ApiClient.createWorkspace({
        name: newWsName.trim(),
        description: newWsDesc.trim() || undefined,
      }, 'tr', authUser);

      setSuccessMsg(`"${created.name}" çalışma alanı başarıyla oluşturuldu.`);
      setNewWsName('');
      setNewWsDesc('');
      setIsCreateModalOpen(false);
      await loadWorkspaces();
      selectWorkspace(created.id);
    } catch (err: any) {
      setErrorMsg(err.message || 'Çalışma alanı oluşturulamadı.');
    }
  };

  const handleInviteMember = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!authUser || !selectedWorkspace || !inviteEmail.trim()) return;
    setErrorMsg(null);
    try {
      await ApiClient.addWorkspaceMember(selectedWorkspace.id, {
        email: inviteEmail.trim(),
        role: inviteRole,
      }, 'tr', authUser);

      setSuccessMsg(`${inviteEmail} takıma ${inviteRole} rolü ile davet edildi.`);
      setInviteEmail('');
      setIsInviteModalOpen(false);
      selectWorkspace(selectedWorkspace.id);
    } catch (err: any) {
      setErrorMsg(err.message || 'Üye davet edilirken hata oluştu.');
    }
  };

  const handleUpdateRole = async (userId: string, newRole: WorkspaceRole) => {
    if (!authUser || !selectedWorkspace) return;
    setErrorMsg(null);
    try {
      await ApiClient.updateWorkspaceMemberRole(selectedWorkspace.id, userId, { role: newRole }, 'tr', authUser);
      setSuccessMsg('Üye rolü başarıyla güncellendi.');
      selectWorkspace(selectedWorkspace.id);
    } catch (err: any) {
      setErrorMsg(err.message || 'Rol güncellenirken hata oluştu.');
    }
  };

  const handleRemoveMember = async (userId: string) => {
    if (!authUser || !selectedWorkspace) return;
    if (!confirm('Bu üyeyi çalışma alanından çıkarmak istediğinizden emin misiniz?')) return;
    setErrorMsg(null);
    try {
      await ApiClient.removeWorkspaceMember(selectedWorkspace.id, userId, 'tr', authUser);
      setSuccessMsg('Üye çalışma alanından çıkarıldı.');
      selectWorkspace(selectedWorkspace.id);
    } catch (err: any) {
      setErrorMsg(err.message || 'Üye çıkarılırken hata oluştu.');
    }
  };

  const isCurrentAdmin = selectedWorkspace?.currentUserRole === 'ADMIN';

  return (
    <div className="space-y-6">
      {/* Top Bar: Selector & Create Button */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-zinc-900/60 p-4 rounded-xl border border-zinc-800">
        <div className="flex items-center gap-3">
          <div className="p-2.5 bg-blue-500/10 text-blue-400 rounded-lg border border-blue-500/20">
            <Building2 className="w-5 h-5" />
          </div>
          <div>
            <h2 className="text-base font-semibold text-white">Çalışma Alanı & Takım Yönetimi</h2>
            <p className="text-xs text-zinc-400">Şirketinizin linklerini yönetin ve ekiplerinize güvenli roller atayın</p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {workspaces.length > 0 && (
            <select
              value={selectedWorkspace?.id || ''}
              onChange={(e) => selectWorkspace(e.target.value)}
              className="bg-zinc-800 border border-zinc-700 text-white text-sm rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
            >
              {workspaces.map((ws) => (
                <option key={ws.id} value={ws.id}>
                  {ws.name} ({ws.currentUserRole})
                </option>
              ))}
            </select>
          )}

          <Button
            onClick={() => setIsCreateModalOpen(true)}
            className="bg-blue-600 hover:bg-blue-500 text-white gap-2 text-xs"
          >
            <Plus className="w-4 h-4" /> Yeni Çalışma Alanı
          </Button>
        </div>
      </div>

      {/* Notifications */}
      {errorMsg && (
        <div className="p-3 bg-red-950/40 border border-red-500/30 rounded-lg flex items-center gap-2 text-xs text-red-300">
          <AlertCircle className="w-4 h-4 text-red-400 shrink-0" />
          <span>{errorMsg}</span>
        </div>
      )}
      {successMsg && (
        <div className="p-3 bg-emerald-950/40 border border-emerald-500/30 rounded-lg flex items-center gap-2 text-xs text-emerald-300">
          <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
          <span>{successMsg}</span>
        </div>
      )}

      {/* Selected Workspace Content */}
      {selectedWorkspace ? (
        <div className="space-y-6">
          {/* Header Card & Stats */}
          <Card className="bg-zinc-900/60 border-zinc-800">
            <CardContent className="p-6">
              <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                  <div className="flex items-center gap-3">
                    <h3 className="text-xl font-bold text-white">{selectedWorkspace.name}</h3>
                    <Badge
                      className={
                        selectedWorkspace.currentUserRole === 'ADMIN'
                          ? 'bg-purple-500/20 text-purple-400 border-purple-500/30'
                          : selectedWorkspace.currentUserRole === 'MEMBER'
                          ? 'bg-blue-500/20 text-blue-400 border-blue-500/30'
                          : 'bg-zinc-500/20 text-zinc-400 border-zinc-500/30'
                      }
                    >
                      {selectedWorkspace.currentUserRole === 'ADMIN' && <Crown className="w-3 h-3 mr-1" />}
                      {selectedWorkspace.currentUserRole === 'MEMBER' && <UserCheck className="w-3 h-3 mr-1" />}
                      {selectedWorkspace.currentUserRole === 'VIEWER' && <Eye className="w-3 h-3 mr-1" />}
                      Rolünüz: {selectedWorkspace.currentUserRole}
                    </Badge>
                  </div>
                  <p className="text-sm text-zinc-400 mt-1">
                    {selectedWorkspace.description || 'Açıklama belirtilmemiş.'}
                  </p>
                </div>

                <div className="flex items-center gap-4">
                  <div className="text-center px-4 py-2 bg-zinc-800/60 rounded-lg border border-zinc-700/50">
                    <div className="text-lg font-bold text-white">{selectedWorkspace.memberCount}</div>
                    <div className="text-xs text-zinc-400">Üye</div>
                  </div>
                  <div className="text-center px-4 py-2 bg-zinc-800/60 rounded-lg border border-zinc-700/50">
                    <div className="text-lg font-bold text-white">{workspaceUrls.length}</div>
                    <div className="text-xs text-zinc-400">Ekip Linki</div>
                  </div>
                  {isCurrentAdmin && (
                    <Button
                      onClick={() => setIsInviteModalOpen(true)}
                      className="bg-emerald-600 hover:bg-emerald-500 text-white gap-2 text-xs"
                    >
                      <UserPlus className="w-4 h-4" /> Üye Davet Et
                    </Button>
                  )}
                </div>
              </div>

              {/* Security Isolation Notice */}
              <div className="mt-5 p-3.5 bg-purple-950/20 border border-purple-500/30 rounded-xl flex items-start gap-3">
                <Shield className="w-5 h-5 text-purple-400 shrink-0 mt-0.5" />
                <div className="text-xs text-zinc-300">
                  <strong className="text-purple-300">Güvenlik ve Rol İzolasyonu: </strong>
                  Bu çalışma alanındaki yöneticilik (<code className="text-purple-300 font-mono">WORKSPACE_ADMIN</code>) yetkisi sadece bu şirkete/ekibe aittir. Ana sistem yöneticiliği (<code className="text-emerald-300 font-mono">ROLE_SUPER_ADMIN</code>) yetkilerinden tamamen izole edilmiştir. Şirket yöneticileri sunucu altyapısına veya diğer şirketlerin verilerine müdahale edemez.
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Sub Navigation Tabs */}
          <div className="flex items-center gap-2 border-b border-zinc-800 pb-2">
            <button
              onClick={() => setActiveTab('members')}
              className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                activeTab === 'members'
                  ? 'bg-zinc-800 text-white border border-zinc-700'
                  : 'text-zinc-400 hover:text-white'
              }`}
            >
              <Users className="w-3.5 h-3.5" /> Ekip Üyeleri ({selectedWorkspace.members?.length || 0})
            </button>
            <button
              onClick={() => setActiveTab('urls')}
              className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                activeTab === 'urls'
                  ? 'bg-zinc-800 text-white border border-zinc-700'
                  : 'text-zinc-400 hover:text-white'
              }`}
            >
              <LinkIcon className="w-3.5 h-3.5" /> Çalışma Alanı Linkleri ({workspaceUrls.length})
            </button>
            <button
              onClick={() => setActiveTab('matrix')}
              className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                activeTab === 'matrix'
                  ? 'bg-zinc-800 text-white border border-zinc-700'
                  : 'text-zinc-400 hover:text-white'
              }`}
            >
              <ShieldAlert className="w-3.5 h-3.5 text-amber-400" /> 🛡️ İzinler & Güvenlik Matrisi
            </button>
          </div>

          {/* Tab 1: Members Table */}
          {activeTab === 'members' && (
            <Card className="bg-zinc-900/60 border-zinc-800">
              <CardHeader className="pb-3">
                <CardTitle className="text-sm font-semibold text-white">Takım Üyeleri ve Yetkiler (RBAC)</CardTitle>
                <CardDescription className="text-xs text-zinc-400">
                  ADMIN (Yönetici), MEMBER (Link Oluşturucu) ve VIEWER (Salt Okunur)
                </CardDescription>
              </CardHeader>
              <CardContent className="p-0">
                <Table>
                  <TableHeader>
                    <TableRow className="border-zinc-800 hover:bg-transparent">
                      <TableHead className="text-zinc-400 text-xs">Kullanıcı</TableHead>
                      <TableHead className="text-zinc-400 text-xs">E-posta</TableHead>
                      <TableHead className="text-zinc-400 text-xs">Rol</TableHead>
                      <TableHead className="text-zinc-400 text-xs">Katılım Tarihi</TableHead>
                      {isCurrentAdmin && <TableHead className="text-right text-zinc-400 text-xs">İşlem</TableHead>}
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {selectedWorkspace.members && selectedWorkspace.members.length > 0 ? (
                      selectedWorkspace.members.map((member) => (
                        <TableRow key={member.userId} className="border-zinc-800/60 hover:bg-zinc-800/30">
                          <TableCell className="text-xs font-medium text-white flex items-center gap-2">
                            {member.role === 'ADMIN' && <Crown className="w-3.5 h-3.5 text-purple-400" />}
                            {member.username}
                          </TableCell>
                          <TableCell className="text-xs text-zinc-400">{member.email}</TableCell>
                          <TableCell>
                            {isCurrentAdmin ? (
                              <select
                                value={member.role}
                                onChange={(e) => handleUpdateRole(member.userId, e.target.value as WorkspaceRole)}
                                className="bg-zinc-800 border border-zinc-700 text-xs text-white rounded px-2 py-1 focus:ring-1 focus:ring-blue-500"
                              >
                                <option value="ADMIN">ADMIN</option>
                                <option value="MEMBER">MEMBER</option>
                                <option value="VIEWER">VIEWER</option>
                              </select>
                            ) : (
                              <Badge className="text-xs bg-zinc-800 text-zinc-300 border-zinc-700">
                                {member.role}
                              </Badge>
                            )}
                          </TableCell>
                          <TableCell className="text-xs text-zinc-500">
                            {new Date(member.joinedAt).toLocaleDateString('tr-TR')}
                          </TableCell>
                          {isCurrentAdmin && (
                            <TableCell className="text-right">
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handleRemoveMember(member.userId)}
                                className="h-7 w-7 p-0 text-zinc-400 hover:text-red-400 hover:bg-red-500/10"
                                title="Üyeyi Çıkar"
                              >
                                <Trash2 className="w-3.5 h-3.5" />
                              </Button>
                            </TableCell>
                          )}
                        </TableRow>
                      ))
                    ) : (
                      <TableRow>
                        <TableCell colSpan={5} className="text-center py-6 text-xs text-zinc-500">
                          Henüz üye bulunmuyor.
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          )}

          {/* Tab 2: Workspace URLs */}
          {activeTab === 'urls' && (
            <Card className="bg-zinc-900/60 border-zinc-800">
              <CardHeader className="pb-3">
                <CardTitle className="text-sm font-semibold text-white">Çalışma Alanındaki Linkler</CardTitle>
                <CardDescription className="text-xs text-zinc-400">
                  Bu takıma atanmış tüm kısa bağlantılar ve performans istatistikleri
                </CardDescription>
              </CardHeader>
              <CardContent className="p-0">
                <Table>
                  <TableHeader>
                    <TableRow className="border-zinc-800 hover:bg-transparent">
                      <TableHead className="text-zinc-400 text-xs">Kısa Link</TableHead>
                      <TableHead className="text-zinc-400 text-xs">Orijinal Hedef</TableHead>
                      <TableHead className="text-zinc-400 text-xs">Tıklama</TableHead>
                      <TableHead className="text-zinc-400 text-xs">Durum</TableHead>
                      <TableHead className="text-right text-zinc-400 text-xs">Tarih</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {workspaceUrls.length > 0 ? (
                      workspaceUrls.map((url) => (
                        <TableRow key={url.shortCode} className="border-zinc-800/60 hover:bg-zinc-800/30">
                          <TableCell className="text-xs font-mono text-blue-400 font-medium">
                            <a href={url.shortUrl} target="_blank" rel="noreferrer" className="hover:underline flex items-center gap-1">
                              {url.shortCode} <ExternalLink className="w-3 h-3 text-zinc-500" />
                            </a>
                          </TableCell>
                          <TableCell className="text-xs text-zinc-300 max-w-xs truncate" title={url.originalUrl}>
                            {url.originalUrl}
                          </TableCell>
                          <TableCell className="text-xs font-semibold text-white">
                            {url.clickCount}
                          </TableCell>
                          <TableCell>
                            <Badge
                              className={`text-[10px] ${
                                url.healthStatus === 'HEALTHY'
                                  ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30'
                                  : 'bg-zinc-800 text-zinc-400'
                              }`}
                            >
                              {url.healthStatus || 'AKTİF'}
                            </Badge>
                          </TableCell>
                          <TableCell className="text-right text-xs text-zinc-500">
                            {new Date(url.createdAt).toLocaleDateString('tr-TR')}
                          </TableCell>
                        </TableRow>
                      ))
                    ) : (
                      <TableRow>
                        <TableCell colSpan={5} className="text-center py-8 text-xs text-zinc-500">
                          Bu çalışma alanına henüz atanmış link bulunmamaktadır. Link kısaltırken bu çalışma alanını seçebilirsiniz.
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          )}

          {/* Tab 3: Granular Permission Matrix */}
          {activeTab === 'matrix' && permissionMatrix && (
            <Card className="bg-zinc-900/60 border-zinc-800">
              <CardHeader className="pb-3 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                  <CardTitle className="text-sm font-semibold text-white flex items-center gap-2">
                    <ShieldAlert className="w-4 h-4 text-amber-400" /> İnce Taneli Yetkilendirme & Güvenlik Matrisi (RBAC)
                  </CardTitle>
                  <CardDescription className="text-xs text-zinc-400">
                    MEMBER ve VIEWER rollerine atanmış işlevsel izinleri özelleştirin.
                  </CardDescription>
                </div>
                <div className="flex items-center gap-2">
                  <Badge className="bg-emerald-500/10 text-emerald-400 border-emerald-500/20 text-[11px] gap-1 py-1">
                    <Zap className="w-3 h-3 text-emerald-400" /> Redis Sub-1ms Cache
                  </Badge>
                  {isCurrentAdmin && (
                    <Button
                      onClick={handleSaveMatrix}
                      disabled={savingMatrix}
                      className="bg-blue-600 hover:bg-blue-500 text-white gap-1.5 text-xs h-8"
                    >
                      <Save className="w-3.5 h-3.5" />
                      {savingMatrix ? 'Kaydediliyor...' : 'İzinleri Kaydet'}
                    </Button>
                  )}
                </div>
              </CardHeader>
              <CardContent className="p-0">
                <Table>
                  <TableHeader>
                    <TableRow className="border-zinc-800 hover:bg-transparent">
                      <TableHead className="text-zinc-400 text-xs w-2/5">İşlevsel İzin / Eylem</TableHead>
                      <TableHead className="text-center text-zinc-400 text-xs">
                        <div className="flex items-center justify-center gap-1">
                          <Crown className="w-3.5 h-3.5 text-purple-400" /> ADMIN
                        </div>
                      </TableHead>
                      <TableHead className="text-center text-zinc-400 text-xs">
                        <div className="flex items-center justify-center gap-1">
                          <UserCheck className="w-3.5 h-3.5 text-blue-400" /> MEMBER
                        </div>
                      </TableHead>
                      <TableHead className="text-center text-zinc-400 text-xs">
                        <div className="flex items-center justify-center gap-1">
                          <Eye className="w-3.5 h-3.5 text-zinc-400" /> VIEWER
                        </div>
                      </TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {[
                      { key: 'canCreateLink', label: 'Yeni Kısa Link Oluşturma', desc: 'Çalışma alanı adına yeni bağlantılar ve alias üretebilir' },
                      { key: 'canDeleteLink', label: 'Link Silme', desc: 'Çalışma alanındaki kısa bağlantıları kalıcı olarak silebilir' },
                      { key: 'canExportReports', label: 'Rapor İndirme (CSV/PDF)', desc: 'Tıklama analitiği ve performans raporlarını dışa aktarabilir' },
                      { key: 'canCustomizeQr', label: 'Dinamik QR Kod Tasarımı', desc: 'Özel renk, desen ve logolu QR kodlar üretebilir' },
                      { key: 'canManageWebhooks', label: 'Webhook Entegrasyonu', desc: 'Tıklama ve güvenlik olayları için hedef webhook tanımlayabilir' },
                      { key: 'canViewAnalytics', label: 'Analitik ve Telemetriyi İzleme', desc: 'Tıklama grafikleri ve coğrafi dağılımı inceleyebilir' },
                    ].map((perm) => (
                      <TableRow key={perm.key} className="border-zinc-800/60 hover:bg-zinc-800/30">
                        <TableCell>
                          <div className="text-xs font-medium text-white">{perm.label}</div>
                          <div className="text-[11px] text-zinc-500">{perm.desc}</div>
                        </TableCell>
                        <TableCell className="text-center">
                          <span className="inline-flex items-center justify-center px-2 py-0.5 rounded text-[10px] font-semibold bg-purple-500/20 text-purple-300 border border-purple-500/30">
                            <Lock className="w-2.5 h-2.5 mr-1" /> Her Zaman Açık
                          </span>
                        </TableCell>
                        <TableCell className="text-center">
                          <input
                            type="checkbox"
                            disabled={!isCurrentAdmin}
                            checked={permissionMatrix.member[perm.key as keyof RolePermissionDto]}
                            onChange={() => handleToggleMatrixPerm('member', perm.key as keyof RolePermissionDto)}
                            className="w-4 h-4 rounded border-zinc-700 bg-zinc-800 text-blue-600 focus:ring-blue-500 focus:ring-offset-zinc-900 cursor-pointer disabled:cursor-not-allowed"
                          />
                        </TableCell>
                        <TableCell className="text-center">
                          <input
                            type="checkbox"
                            disabled={!isCurrentAdmin}
                            checked={permissionMatrix.viewer[perm.key as keyof RolePermissionDto]}
                            onChange={() => handleToggleMatrixPerm('viewer', perm.key as keyof RolePermissionDto)}
                            className="w-4 h-4 rounded border-zinc-700 bg-zinc-800 text-blue-600 focus:ring-blue-500 focus:ring-offset-zinc-900 cursor-pointer disabled:cursor-not-allowed"
                          />
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          )}
        </div>
      ) : (
        <div className="p-8 text-center bg-zinc-900/40 rounded-xl border border-dashed border-zinc-800">
          <Building2 className="w-10 h-10 text-zinc-600 mx-auto mb-3" />
          <h4 className="text-sm font-semibold text-white">Henüz Bir Çalışma Alanınız Yok</h4>
          <p className="text-xs text-zinc-400 max-w-md mx-auto mt-1 mb-4">
            Ekibinizle linkleri ortak havuzda yönetmek ve rol bazlı yetkilendirme yapmak için hemen bir çalışma alanı oluşturun.
          </p>
          <Button
            onClick={() => setIsCreateModalOpen(true)}
            className="bg-blue-600 hover:bg-blue-500 text-white gap-2 text-xs"
          >
            <Plus className="w-4 h-4" /> Çalışma Alanı Oluştur
          </Button>
        </div>
      )}

      {/* Modal 1: Create Workspace */}
      <Dialog open={isCreateModalOpen} onOpenChange={setIsCreateModalOpen}>
        <DialogContent className="bg-zinc-900 border-zinc-800 text-white sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="text-base font-bold flex items-center gap-2">
              <Building2 className="w-5 h-5 text-blue-400" /> Yeni Çalışma Alanı Oluştur
            </DialogTitle>
            <DialogDescription className="text-xs text-zinc-400">
              Şirketiniz veya ekibiniz için bağımsız bir çalışma alanı oluşturun. Otomatik olarak yönetici atanacaksınız.
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={handleCreateWorkspace} className="space-y-4 mt-2">
            <div>
              <label className="text-xs font-medium text-zinc-300">Çalışma Alanı Adı *</label>
              <Input
                value={newWsName}
                onChange={(e) => setNewWsName(e.target.value)}
                placeholder="Örn: Pazarlama Departmanı"
                required
                className="mt-1 bg-zinc-800 border-zinc-700 text-white text-xs"
              />
            </div>
            <div>
              <label className="text-xs font-medium text-zinc-300">Açıklama (İsteğe bağlı)</label>
              <Input
                value={newWsDesc}
                onChange={(e) => setNewWsDesc(e.target.value)}
                placeholder="Örn: 2026 kampanya linkleri ve bio sayfaları"
                className="mt-1 bg-zinc-800 border-zinc-700 text-white text-xs"
              />
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <Button
                type="button"
                variant="outline"
                onClick={() => setIsCreateModalOpen(false)}
                className="text-xs border-zinc-700 text-zinc-300 hover:bg-zinc-800"
              >
                İptal
              </Button>
              <Button type="submit" className="text-xs bg-blue-600 hover:bg-blue-500 text-white">
                Oluştur
              </Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>

      {/* Modal 2: Invite Member */}
      <Dialog open={isInviteModalOpen} onOpenChange={setIsInviteModalOpen}>
        <DialogContent className="bg-zinc-900 border-zinc-800 text-white sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="text-base font-bold flex items-center gap-2">
              <UserPlus className="w-5 h-5 text-emerald-400" /> Ekip Üyesi Davet Et
            </DialogTitle>
            <DialogDescription className="text-xs text-zinc-400">
              Kullanıcının sistemde kayıtlı e-posta adresini girerek çalışma alanına dahil edin.
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={handleInviteMember} className="space-y-4 mt-2">
            <div>
              <label className="text-xs font-medium text-zinc-300">Kullanıcı E-posta Adresi *</label>
              <Input
                type="email"
                value={inviteEmail}
                onChange={(e) => setInviteEmail(e.target.value)}
                placeholder="calisan@sirket.com"
                required
                className="mt-1 bg-zinc-800 border-zinc-700 text-white text-xs"
              />
            </div>
            <div>
              <label className="text-xs font-medium text-zinc-300">Yetki Rolü *</label>
              <select
                value={inviteRole}
                onChange={(e) => setInviteRole(e.target.value as WorkspaceRole)}
                className="w-full mt-1 bg-zinc-800 border border-zinc-700 text-white text-xs rounded-lg px-3 py-2 focus:ring-1 focus:ring-blue-500"
              >
                <option value="MEMBER">MEMBER — Link oluşturabilir, kendi linklerini düzenleyebilir</option>
                <option value="VIEWER">VIEWER — Sadece linkleri ve analitikleri görüntüleyebilir (Salt Okunur)</option>
                <option value="ADMIN">ADMIN — Şirket yöneticisi (Üye davet edebilir, rolleri yönetebilir)</option>
              </select>
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <Button
                type="button"
                variant="outline"
                onClick={() => setIsInviteModalOpen(false)}
                className="text-xs border-zinc-700 text-zinc-300 hover:bg-zinc-800"
              >
                İptal
              </Button>
              <Button type="submit" className="text-xs bg-emerald-600 hover:bg-emerald-500 text-white">
                Davet Et
              </Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
