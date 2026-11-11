from balepy import Client
from balepy.types import Updates
from asyncio import run
import random

TOKEN = "2061712189:APdJaI8ZUnrc8vZ8gk461wdZXpsois1_ROU"

app = Client("test chatbot", bot_token=TOKEN)

replies = [
    "خسته‌ام ولم کن 😒",
    "حوصله ندارم، برو بعداً بیا! 🙄",
    "باز شروع کردی؟ ولم کن 😂"
]

@app.on_message
async def updates(update: Updates):
    if update.text:
        answer = random.choice(replies)
        await app.send_message(
            update.chat_id,
            answer,
            update.message_id
        )

run(updates())