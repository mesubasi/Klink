.PHONY: help up dev down stop stop-all kill kill-all restart logs logs-backend logs-frontend ps clean

# Varsayilan komut: yardim menusunu gosterir
help:
	@echo ========================================================
	@echo   Klink URL Shortener - Proje Yonetim Komutlari
	@echo ========================================================
	@echo   make up             - Projeyi PRODUCTION modunda derler ve baslatir
	@echo   make dev            - Projeyi DEVELOPMENT (Hot Reload) modunda baslatir
	@echo   make stop           - Calisan servisleri durdurur (silmez)
	@echo   make kill           - Proje servislerini ZORLA aninda kapatir
	@echo   make down           - Servisleri durdurur ve aglari kaldirir
	@echo   make stop-all       - Tum Docker konteynerlerini durdurur (docker stop)
	@echo   make kill-all       - Tum Docker konteynerlerini ZORLA kapatir (docker kill)
	@echo   make restart        - Servisleri yeniden baslatir
	@echo   make logs           - Tum servislerin loglarini canli izler
	@echo   make logs-backend   - Sadece Backend loglarini izler
	@echo   make logs-frontend  - Sadece Frontend loglarini izler
	@echo   make ps             - Calisan konteynerlerin durumunu gosterir
	@echo   make clean          - Konteynerleri ve veritabani hacimlerini siler
	@echo ========================================================

# Projeyi PRODUCTION modunda derleyip arka planda baslatir
up:
	docker compose up -d --build

# Projeyi DEVELOPMENT (Hot Reload) modunda baslatir
dev:
	docker compose -f docker-compose.dev.yml up -d --build

# Proje servislerini gecici olarak durdurur (konteynerler silinmez)
stop:
	docker compose stop

# Proje servislerini aninda ZORLA kapatir (docker compose kill)
kill:
	docker compose kill

# Sistemdeki CALISAN TUM Docker konteynerlerini durdurur (docker stop $$(docker ps -q))
stop-all:
	@docker stop $$(docker ps -q) 2>/dev/null || true

# Sistemdeki CALISAN TUM Docker konteynerlerini ZORLA kapatir (docker kill $$(docker ps -q))
kill-all:
	@docker kill $$(docker ps -q) 2>/dev/null || true

# Calisan tum servisleri durdurur ve konteynerleri kaldirir
down:
	docker compose down

# Servisleri yeniden baslatir
restart:
	docker compose restart

# Tum log akisini takip eder
logs:
	docker compose logs -f

# Sadece backend loglarini takip eder
logs-backend:
	docker compose logs -f backend

# Sadece frontend loglarini takip eder
logs-frontend:
	docker compose logs -f frontend

# Calisan servislerin durumunu listeler
ps:
	docker compose ps

# Tum verileri ve konteynerleri sifirlar
clean:
	docker compose down -v --remove-orphans
