export interface ShortenRequest {
  originalUrl: string;
  customAlias?: string;
  expirationDays?: number;
  expiresAt?: number;
  password?: string;
  previewEnabled?: boolean;
  iosUrl?: string;
  androidUrl?: string;
  desktopUrl?: string;
  webhookUrl?: string;
  webhookSecret?: string;
}

export interface ShortenResponse {
  shortCode: string;
  shortUrl: string;
  originalUrl: string;
  createdAt: number;
  expiresAt?: number;
  clickCount: number;
  passwordProtected: boolean;
  previewEnabled?: boolean;
  iosUrl?: string;
  androidUrl?: string;
  desktopUrl?: string;
  webhookUrl?: string;
}

export interface UrlPreviewResponse {
  shortCode: string;
  shortUrl: string;
  originalUrl: string;
  domain: string;
  protocol: string;
  secure: boolean;
  safetyStatus: 'SAFE' | 'SUSPICIOUS' | 'MALICIOUS' | string;
  safetyScore: number;
  googleSafeBrowsingStatus?: string;
  virusTotalStatus?: string;
  passwordProtected: boolean;
  previewEnabled: boolean;
  createdAt: number;
  expiresAt?: number;
  clickCount: number;
  active: boolean;
  iosUrl?: string;
  androidUrl?: string;
  desktopUrl?: string;
  webhookUrl?: string;
}

export interface BulkShortenRequest {
  urls: ShortenRequest[];
}

export interface BulkShortenResponse {
  totalCount: number;
  successCount: number;
  shortenedUrls: ShortenResponse[];
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  username: string;
  email: string;
  role: string;
  message: string;
  accessToken?: string;
  tokenType?: string;
  twoFactorRequired?: boolean;
}

export interface UserDto {
  id: string;
  username: string;
  email: string;
  role: string;
  twoFactorEnabled?: boolean;
  createdAt: number;
}

export interface TotpSetupResponse {
  secretKey: string;
  qrCodeUrl: string;
  otpAuthUrl: string;
}

export interface TotpVerifyRequest {
  code: string;
  secretKey?: string;
}

export interface TotpLoginRequest {
  username: string;
  password: string;
  code: string;
}

export interface ClickAnalytics {
  id: string;
  shortCode: string;
  clickedAt: number;
  ipAddress: string;
  userAgent: string;
  referrer: string;
  country?: string;
  countryCode?: string;
  city?: string;
  bot?: boolean;
  botCategory?: string;
}

export interface UrlStatsResponse {
  shortCode: string;
  originalUrl: string;
  shortUrl: string;
  createdAt: number;
  expiresAt?: number;
  totalClicks: number;
  recentClicks: ClickAnalytics[];
}

export interface AnalyticsSummaryResponse {
  shortCode: string;
  originalUrl: string;
  totalClicks: number;
  humanClicks?: number;
  botClicks?: number;
  clicksByDevice: Record<string, number>;
  clicksByReferrer: Record<string, number>;
  clicksByDate: Record<string, number>;
  clicksByCountry?: Record<string, number>;
  clicksByCity?: Record<string, number>;
  clicksByBotCategory?: Record<string, number>;
  hourlyHeatmap?: number[][];
}

export interface RedisStatusDto {
  status: 'CONNECTED' | 'DISCONNECTED' | string;
  host: string;
  port: number;
  pingLatencyMs?: number | null;
  totalKeys: number;
  usedMemory: string;
  redisVersion: string;
  uptimeDays: number;
  message: string;
}

export interface RabbitMqStatusDto {
  status: 'CONNECTED' | 'DISCONNECTED' | string;
  host: string;
  port: number;
  virtualHost: string;
  queueName: string;
  messageCount: number;
  consumerCount: number;
  exchangeName: string;
  routingKey: string;
  message: string;
}

export interface SystemStatusResponse {
  overallStatus: 'HEALTHY' | 'DEGRADED' | 'DOWN' | string;
  timestamp: number | string;
  redis: RedisStatusDto;
  rabbitMq: RabbitMqStatusDto;
}

export interface BioLinkItemDto {
  id?: string;
  title: string;
  url: string;
  icon?: string;
  highlighted?: boolean;
  active?: boolean;
  sortOrder?: number;
  clickCount?: number;
}

export interface BioPageDto {
  id?: string;
  username: string;
  displayName: string;
  bioDescription?: string;
  avatarUrl?: string;
  theme?: string;
  socialLinks?: string; // JSON string e.g. {"twitter":"...", "instagram":"...", "github":"..."}
  verified?: boolean;
  viewCount?: number;
  createdAt?: number;
  updatedAt?: number;
  links: BioLinkItemDto[];
}

export interface BioPageUpdateRequest {
  username?: string;
  displayName: string;
  bioDescription?: string;
  avatarUrl?: string;
  theme?: string;
  socialLinks?: string;
  links: BioLinkItemDto[];
}

export type ApiKeyStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'REVOKED';

export interface ApiKeyResponse {
  id: string;
  keyPrefix?: string;
  rawKey?: string;
  appName: string;
  purpose: string;
  websiteUrl?: string;
  expectedMonthlyClicks?: string;
  ipWhitelist?: string;
  status: ApiKeyStatus;
  rejectionReason?: string;
  rateLimitPerMinute: number;
  totalCalls: number;
  lastUsedAt?: number;
  createdAt: number;
  approvedAt?: number;
  username?: string;
  userEmail?: string;
}

export interface ApiKeyApplyRequest {
  appName: string;
  purpose: string;
  websiteUrl?: string;
  expectedMonthlyClicks?: string;
  ipWhitelist?: string;
}

export interface ApiKeyActionRequest {
  rateLimitPerMinute?: number;
  rejectionReason?: string;
}

