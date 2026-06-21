import asyncio
import sys
import time
import random
from aiohttp import ClientSession, ClientTimeout, ClientError
from typing import Optional, Dict, Any

# Цвета для вывода в консоль
GREEN = '\033[92m'
RED = '\033[91m'
YELLOW = '\033[93m'
CYAN = '\033[96m'
RESET = '\033[0m'

class CatDropBot:
    def __init__(self, auth_token: str, quantity: int = 1, delay_seconds: int = 2):
        self.auth_token = auth_token
        self.quantity = quantity
        self.delay_seconds = delay_seconds
        self.base_url = "https://salemfriend.com"
        self.boc_value = "f7d4da81a9d10f0a03f849093530aaa818290347a06b259ca312e9f37c429ba3"
        self.headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {auth_token}"
        }
        
    async def create_intent(self, session: ClientSession) -> Optional[Dict[str, Any]]:
        """Создание интента на покупку билетов"""
        url = f"{self.base_url}/api/cat-drop/ton-intent"
        payload = {"quantity": self.quantity}
        
        try:
            async with session.post(url, json=payload, headers=self.headers) as response:
                if response.status == 200:
                    data = await response.json()
                    print(f"{GREEN}✓ Интент создан: intentId={data.get('intentId')}{RESET}")
                    return data
                else:
                    error_text = await response.text()
                    print(f"{RED}✗ Ошибка создания интента: {response.status} - {error_text}{RESET}")
                    return None
        except ClientError as e:
            print(f"{RED}✗ Сетевая ошибка при создании интента: {e}{RESET}")
            return None
    
    async def confirm_purchase(self, session: ClientSession, intent_id: str) -> Optional[Dict[str, Any]]:
        """Подтверждение покупки с фиксированным BOC"""
        url = f"{self.base_url}/api/cat-drop/ton-confirm"
        payload = {
            "intentId": intent_id,
            "boc": self.boc_value
        }
        
        try:
            async with session.post(url, json=payload, headers=self.headers) as response:
                if response.status == 200:
                    data = await response.json()
                    if data.get("success"):
                        print(f"{GREEN}✓ Покупка подтверждена! Билетов: {data.get('userTickets')}{RESET}")
                        return data
                    else:
                        print(f"{RED}✗ Сервер вернул success=false{RESET}")
                        return None
                else:
                    error_text = await response.text()
                    print(f"{RED}✗ Ошибка подтверждения: {response.status} - {error_text}{RESET}")
                    return None
        except ClientError as e:
            print(f"{RED}✗ Сетевая ошибка при подтверждении: {e}{RESET}")
            return None
    
    async def run_forever(self):
        """Основной бесконечный цикл работы бота"""
        print(f"{YELLOW}🚀 Запуск бота CatDrop...{RESET}")
        print(f"{YELLOW}📋 Токен: {self.auth_token[:15]}...{RESET}")
        print(f"{YELLOW}🎫 Количество билетов за раз: {self.quantity}{RESET}")
        print(f"{YELLOW}⏱️  Задержка между циклами: {self.delay_seconds} сек{RESET}")
        print(f"{YELLOW}🔧 Фиксированный BOC: {self.boc_value[:30]}...{RESET}")
        print(f"{YELLOW}➖" * 50 + f"{RESET}\n")
        
        timeout = ClientTimeout(total=30)
        
        async with ClientSession(timeout=timeout) as session:
            iteration = 0
            success_count = 0
            error_count = 0
            
            while True:
                iteration += 1
                print(f"{CYAN}📌 Итерация #{iteration}{RESET}")
                print(f"{CYAN}📊 Статистика: успешно={success_count}, ошибок={error_count}{RESET}")
                
                # Шаг 1: Создание интента
                intent_data = await self.create_intent(session)
                
                if not intent_data:
                    error_count += 1
                    print(f"{YELLOW}⚠️ Пропускаем подтверждение из-за ошибки интента{RESET}\n")
                    await asyncio.sleep(self.delay_seconds)
                    continue
                
                intent_id = intent_data.get("intentId")
                receiver_address = intent_data.get("receiverAddress")
                amount_nano = intent_data.get("amountNano")
                valid_until = intent_data.get("validUntil")
                payload_base64 = intent_data.get("payloadBase64")
                
                print(f"   ├─ intentId: {intent_id}")
                print(f"   ├─ receiverAddress: {receiver_address}")
                print(f"   ├─ amountNano: {amount_nano}")
                print(f"   ├─ validUntil: {valid_until} ({time.ctime(valid_until) if valid_until else 'N/A'})")
                print(f"   ├─ payloadBase64: {payload_base64[:50] if payload_base64 else 'N/A'}...")
                print(f"   └─ BOC: {self.boc_value[:30]}...")
                
                # Шаг 2: Подтверждение покупки
                confirm_result = await self.confirm_purchase(session, intent_id)
                
                if confirm_result:
                    success_count += 1
                    print(f"{GREEN}✅ УСПЕХ! Итерация #{iteration} завершена успешно{RESET}")
                else:
                    error_count += 1
                    print(f"{RED}❌ НЕУДАЧА! Итерация #{iteration} завершена ошибкой{RESET}")
                
                print(f"{YELLOW}➖" * 50 + f"{RESET}")
                print(f"{YELLOW}⏳ Ожидание {self.delay_seconds} секунд перед следующим циклом...{RESET}\n")
                
                await asyncio.sleep(self.delay_seconds)
    
    async def run_once(self):
        """Выполнить одну итерацию (для тестирования)"""
        timeout = ClientTimeout(total=30)
        
        async with ClientSession(timeout=timeout) as session:
            intent_data = await self.create_intent(session)
            if intent_data:
                intent_id = intent_data.get("intentId")
                await self.confirm_purchase(session, intent_id)

async def main():
    # Получаем токен из аргументов командной строки
    if len(sys.argv) < 2:
        print(f"{RED}❌ Ошибка: Укажите JWT токен как аргумент{RESET}")
        print(f"{YELLOW}Пример использования:{RESET}")
        print(f"  python script.py 'YOUR_JWT_TOKEN'                 # базовая команда")
        print(f"  python script.py 'YOUR_JWT_TOKEN' 3               # 3 билета за раз")
        print(f"  python script.py 'YOUR_JWT_TOKEN' 3 5             # 3 билета, задержка 5 сек")
        print(f"  python script.py 'YOUR_JWT_TOKEN' 1 0             # без задержки")
        sys.exit(1)
    
    auth_token = sys.argv[1]
    
    # Опциональные параметры
    quantity = int(sys.argv[2]) if len(sys.argv) > 2 else 1
    delay = float(sys.argv[3]) if len(sys.argv) > 3 else 2
    
    bot = CatDropBot(auth_token, quantity, delay)
    
    try:
        await bot.run_forever()
    except KeyboardInterrupt:
        print(f"\n{YELLOW}🛑 Бот остановлен пользователем{RESET}")
        print(f"{YELLOW}📈 Итоговая статистика доступна в последнем выводе{RESET}")
    except Exception as e:
        print(f"{RED}💥 Критическая ошибка: {e}{RESET}")
        raise

if __name__ == "__main__":
    asyncio.run(main())
