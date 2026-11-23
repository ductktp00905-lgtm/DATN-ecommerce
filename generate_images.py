import os
import shutil

images = {
    "tv-samsung-crystal.svg": """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 450" width="100%" height="100%">
  <defs>
    <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#0f172a"/>
      <stop offset="100%" stop-color="#1e293b"/>
    </linearGradient>
    <linearGradient id="screen" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#0284c7"/>
      <stop offset="50%" stop-color="#8b5cf6"/>
      <stop offset="100%" stop-color="#ec4899"/>
    </linearGradient>
  </defs>
  <rect width="600" height="450" fill="url(#bg)"/>
  <!-- TV Body -->
  <rect x="70" y="50" width="460" height="280" rx="10" fill="#262626" stroke="#404040" stroke-width="4"/>
  <!-- TV Screen -->
  <rect x="80" y="60" width="440" height="260" rx="6" fill="url(#screen)"/>
  <text x="300" y="180" font-family="Arial, sans-serif" font-size="28" font-weight="900" fill="#ffffff" text-anchor="middle" letter-spacing="2">SAMSUNG CRYSTAL 4K</text>
  <text x="300" y="220" font-family="Arial, sans-serif" font-size="16" font-weight="600" fill="#f1f5f9" text-anchor="middle">Dynamic Crystal Color • Tizen OS</text>
  <!-- Stand -->
  <polygon points="260,330 340,330 355,390 245,390" fill="#333333"/>
  <rect x="220" y="388" width="160" height="12" rx="4" fill="#525252"/>
  <!-- 4K Badge -->
  <rect x="95" y="75" width="60" height="26" rx="4" fill="#000000" opacity="0.6"/>
  <text x="125" y="93" font-family="Arial, sans-serif" font-size="13" font-weight="800" fill="#38bdf8" text-anchor="middle">4K UHD</text>
</svg>""",

    "tv-samsung-crystal-side.svg": """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 450" width="100%" height="100%">
  <rect width="600" height="450" fill="#0f172a"/>
  <!-- Side angle representation -->
  <path d="M180,60 L420,100 L420,340 L180,310 Z" fill="#1e293b" stroke="#38bdf8" stroke-width="3"/>
  <text x="300" y="210" font-family="Arial, sans-serif" font-size="22" font-weight="800" fill="#ffffff" text-anchor="middle">AirSlim Design</text>
  <text x="300" y="240" font-family="Arial, sans-serif" font-size="14" fill="#94a3b8" text-anchor="middle">Độ mỏng ấn tượng chỉ 2.5cm</text>
  <!-- Stand angle -->
  <polygon points="270,325 330,335 340,390 260,385" fill="#475569"/>
</svg>""",

    "tv-samsung-crystal-room.svg": """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 450" width="100%" height="100%">
  <rect width="600" height="450" fill="#1e1e24"/>
  <!-- Wall mount room setup -->
  <rect x="100" y="40" width="400" height="240" rx="8" fill="#111" stroke="#333" stroke-width="2"/>
  <text x="300" y="155" font-family="Arial, sans-serif" font-size="20" font-weight="bold" fill="#60a5fa" text-anchor="middle">Không gian phòng khách hiện đại</text>
  <!-- Console table -->
  <rect x="60" y="320" width="480" height="60" rx="4" fill="#3f3f46"/>
  <rect x="180" y="300" width="240" height="20" rx="4" fill="#18181b"/>
  <text x="300" y="315" font-family="Arial, sans-serif" font-size="11" fill="#a1a1aa" text-anchor="middle">Q-Symphony Soundbar Setup</text>
</svg>""",

    "tv-sony-bravia.svg": """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 450" width="100%" height="100%">
  <defs>
    <linearGradient id="sonyScreen" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#1e1b4b"/>
      <stop offset="60%" stop-color="#312e81"/>
      <stop offset="100%" stop-color="#4338ca"/>
    </linearGradient>
  </defs>
  <rect width="600" height="450" fill="#090d16"/>
  <rect x="60" y="50" width="480" height="290" rx="8" fill="#171717" stroke="#262626" stroke-width="5"/>
  <rect x="72" y="62" width="456" height="266" rx="4" fill="url(#sonyScreen)"/>
  <text x="300" y="180" font-family="Arial, sans-serif" font-size="28" font-weight="900" fill="#ffffff" text-anchor="middle" letter-spacing="3">SONY BRAVIA 4K</text>
  <text x="300" y="215" font-family="Arial, sans-serif" font-size="15" font-weight="600" fill="#cbd5e1" text-anchor="middle">Google TV • X-Reality PRO • Dolby Atmos</text>
  <!-- Stand legs -->
  <line x1="140" y1="340" x2="110" y2="395" stroke="#737373" stroke-width="8" stroke-linecap="round"/>
  <line x1="460" y1="340" x2="490" y2="395" stroke="#737373" stroke-width="8" stroke-linecap="round"/>
  <rect x="440" y="75" width="75" height="24" rx="4" fill="#000" opacity="0.7"/>
  <text x="477" y="91" font-family="Arial, sans-serif" font-size="11" font-weight="800" fill="#e2e8f0" text-anchor="middle">google tv</text>
</svg>""",

    "tv-sony-bravia-angle.svg": """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 450" width="100%" height="100%">
  <rect width="600" height="450" fill="#0f172a"/>
  <circle cx="300" cy="225" r="140" fill="#1e1b4b" opacity="0.6"/>
  <text x="300" y="210" font-family="Arial, sans-serif" font-size="24" font-weight="bold" fill="#ffffff" text-anchor="middle">X-Protection PRO</text>
  <text x="300" y="245" font-family="Arial, sans-serif" font-size="14" fill="#94a3b8" text-anchor="middle">Chống bụi bẩn, độ ẩm, sốc điện và sấm sét</text>
</svg>""",

    "soundbar-samsung.svg": """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 450" width="100%" height="100%">
  <rect width="600" height="450" fill="#0f172a"/>
  <!-- Soundbar bar -->
  <rect x="50" y="220" width="500" height="55" rx="8" fill="#18181b" stroke="#3f3f46" stroke-width="3"/>
  <!-- Speaker mesh dots -->
  <line x1="70" y1="247" x2="530" y2="247" stroke="#52525b" stroke-width="2" stroke-dasharray="4,4"/>
  <!-- Subwoofer -->
  <rect x="230" y="80" width="140" height="120" rx="8" fill="#18181b" stroke="#3f3f46" stroke-width="3"/>
  <circle cx="300" cy="140" r="38" fill="#27272a" stroke="#06b6d4" stroke-width="3"/>
  <text x="300" y="320" font-family="Arial, sans-serif" font-size="22" font-weight="bold" fill="#ffffff" text-anchor="middle">Samsung Q-Series Soundbar</text>
  <text x="300" y="350" font-family="Arial, sans-serif" font-size="14" fill="#38bdf8" text-anchor="middle">Dolby Atmos Không Dây • 3.1.2 Kênh • 360W</text>
</svg>""",

    "fridge-lg-sidebyside.svg": """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 450" width="100%" height="100%">
  <rect width="600" height="450" fill="#f1f5f9"/>
  <!-- Fridge Body -->
  <rect x="180" y="40" width="240" height="350" rx="10" fill="#334155" stroke="#1e293b" stroke-width="4"/>
  <!-- Doors split -->
  <line x1="300" y1="40" x2="300" y2="390" stroke="#0f172a" stroke-width="3"/>
  <!-- Handles -->
  <rect x="286" y="160" width="6" height="80" rx="3" fill="#94a3b8"/>
  <rect x="308" y="160" width="6" height="80" rx="3" fill="#94a3b8"/>
  <!-- Water dispenser display -->
  <rect x="210" y="120" width="60" height="70" rx="4" fill="#1e293b"/>
  <rect x="220" y="130" width="40" height="15" rx="2" fill="#0284c7"/>
  <!-- Badge -->
  <text x="300" y="420" font-family="Arial, sans-serif" font-size="18" font-weight="900" fill="#0f172a" text-anchor="middle">LG SIDE BY SIDE 519L</text>
  <text x="300" y="440" font-family="Arial, sans-serif" font-size="12" font-weight="600" fill="#64748b" text-anchor="middle">Smart Inverter • Multi Air Flow</text>
</svg>""",

    "fridge-lg-open.svg": """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 450" width="100%" height="100%">
  <rect width="600" height="450" fill="#f8fafc"/>
  <rect x="160" y="50" width="280" height="340" rx="8" fill="#e2e8f0" stroke="#94a3b8" stroke-width="3"/>
  <line x1="300" y1="50" x2="300" y2="390" stroke="#64748b" stroke-width="2"/>
  <!-- Interior shelves -->
  <line x1="180" y1="120" x2="280" y2="120" stroke="#38bdf8" stroke-width="3"/>
  <line x1="180" y1="190" x2="280" y2="190" stroke="#38bdf8" stroke-width="3"/>
  <line x1="180" y1="260" x2="280" y2="260" stroke="#38bdf8" stroke-width="3"/>
  <line x1="320" y1="120" x2="420" y2="120" stroke="#38bdf8" stroke-width="3"/>
  <line x1="320" y1="190" x2="420" y2="190" stroke="#38bdf8" stroke-width="3"/>
  <line x1="320" y1="260" x2="420" y2="260" stroke="#38bdf8" stroke-width="3"/>
  <text x="300" y="425" font-family="Arial, sans-serif" font-size="16" font-weight="bold" fill="#0f172a" text-anchor="middle">Khay kính chịu lực 150kg</text>
</svg>""",

    "fridge-panasonic.svg": """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 450" width="100%" height="100%">
  <rect width="600" height="450" fill="#f1f5f9"/>
  <!-- Panasonic 2-door Bottom Freezer -->
  <rect x="200" y="40" width="200" height="350" rx="8" fill="#475569" stroke="#334155" stroke-width="4"/>
  <!-- Top door (fridge) -->
  <rect x="205" y="45" width="190" height="210" rx="4" fill="#64748b"/>
  <!-- Bottom door (freezer) -->
  <rect x="205" y="260" width="190" height="125" rx="4" fill="#475569"/>
  <!-- Prime Fresh label -->
  <rect x="240" y="195" width="120" height="25" rx="4" fill="#0284c7"/>
  <text x="300" y="212" font-family="Arial, sans-serif" font-size="10" font-weight="bold" fill="#ffffff" text-anchor="middle">PRIME FRESH -3°C</text>
  <text x="300" y="420" font-family="Arial, sans-serif" font-size="18" font-weight="900" fill="#0f172a" text-anchor="middle">PANASONIC 322L INVERTER</text>
  <text x="300" y="440" font-family="Arial, sans-serif" font-size="12" font-weight="600" fill="#64748b" text-anchor="middle">Econavi Cảm Biến Thông Minh • Ag Clean</text>
</svg>""",

    "washing-machine-lg.svg": """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 450" width="100%" height="100%">
  <rect width="600" height="450" fill="#f8fafc"/>
  <!-- Outer Body -->
  <rect x="180" y="40" width="240" height="330" rx="12" fill="#ffffff" stroke="#cbd5e1" stroke-width="4"/>
  <!-- Top Panel -->
  <rect x="190" y="55" width="220" height="45" rx="4" fill="#f1f5f9"/>
  <!-- Knob -->
  <circle cx="300" cy="77" r="14" fill="#94a3b8" stroke="#64748b" stroke-width="2"/>
  <!-- LED Display -->
  <rect x="330" y="65" width="65" height="25" rx="3" fill="#0f172a"/>
  <text x="362" y="82" font-family="monospace" font-size="12" font-weight="bold" fill="#38bdf8" text-anchor="middle">18:00</text>
  <!-- Circular Door -->
  <circle cx="300" cy="225" r="90" fill="#0f172a" stroke="#475569" stroke-width="6"/>
  <circle cx="300" cy="225" r="70" fill="#1e293b"/>
  <circle cx="300" cy="225" r="50" fill="#0284c7" opacity="0.4"/>
  <!-- Text -->
  <text x="300" y="405" font-family="Arial, sans-serif" font-size="18" font-weight="900" fill="#0f172a" text-anchor="middle">LG AI DD INVERTER</text>
  <text x="300" y="425" font-family="Arial, sans-serif" font-size="12" font-weight="600" fill="#64748b" text-anchor="middle">Giặt hơi nước Steam diệt khuẩn 99.9%</text>
</svg>""",

    "washing-machine-lg-open.svg": """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 450" width="100%" height="100%">
  <rect width="600" height="450" fill="#f8fafc"/>
  <circle cx="300" cy="200" r="120" fill="#cbd5e1" stroke="#64748b" stroke-width="4"/>
  <circle cx="300" cy="200" r="95" fill="#334155"/>
  <text x="300" y="195" font-family="Arial, sans-serif" font-size="18" font-weight="bold" fill="#ffffff" text-anchor="middle">Lồng giặt thép không gỉ</text>
  <text x="300" y="225" font-family="Arial, sans-serif" font-size="13" fill="#38bdf8" text-anchor="middle">Bảo vệ sợi vải tối ưu 18%</text>
  <text x="300" y="380" font-family="Arial, sans-serif" font-size="16" font-weight="800" fill="#0f172a" text-anchor="middle">Động cơ truyền động trực tiếp AI DD</text>
</svg>""",

    "washing-machine-electrolux.svg": """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 450" width="100%" height="100%">
  <rect width="600" height="450" fill="#f1f5f9"/>
  <!-- Electrolux Washer-Dryer -->
  <rect x="180" y="40" width="240" height="330" rx="12" fill="#334155" stroke="#1e293b" stroke-width="4"/>
  <rect x="190" y="55" width="220" height="45" rx="4" fill="#1e293b"/>
  <circle cx="300" cy="77" r="14" fill="#cbd5e1"/>
  <circle cx="300" cy="225" r="90" fill="#0f172a" stroke="#cbd5e1" stroke-width="5"/>
  <circle cx="300" cy="225" r="68" fill="#1e293b"/>
  <text x="300" y="405" font-family="Arial, sans-serif" font-size="18" font-weight="900" fill="#0f172a" text-anchor="middle">ELECTROLUX ULTIMATECARE</text>
  <text x="300" y="425" font-family="Arial, sans-serif" font-size="12" font-weight="600" fill="#64748b" text-anchor="middle">Giặt 10kg - Sấy 7kg • SensorWash • UltraMix</text>
</svg>""",

    "ac-daikin-inverter.svg": """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 450" width="100%" height="100%">
  <rect width="600" height="450" fill="#f8fafc"/>
  <!-- Indoor AC Unit -->
  <rect x="80" y="130" width="440" height="150" rx="12" fill="#ffffff" stroke="#cbd5e1" stroke-width="4"/>
  <!-- Front seam line -->
  <line x1="85" y1="230" x2="515" y2="230" stroke="#e2e8f0" stroke-width="2"/>
  <!-- Air vent flap -->
  <rect x="110" y="240" width="380" height="24" rx="4" fill="#f1f5f9" stroke="#94a3b8" stroke-width="1"/>
  <!-- Brand logo -->
  <text x="130" y="165" font-family="Arial, sans-serif" font-size="14" font-weight="900" fill="#0284c7" letter-spacing="2">DAIKIN</text>
  <!-- Display temperature -->
  <rect x="420" y="150" width="55" height="30" rx="4" fill="#0f172a"/>
  <text x="447" y="172" font-family="monospace" font-size="16" font-weight="bold" fill="#38bdf8" text-anchor="middle">24°C</text>
  <!-- Wind breeze effects -->
  <path d="M140,280 Q200,340 260,320 T380,350 T500,320" fill="none" stroke="#38bdf8" stroke-width="3" stroke-dasharray="6,4"/>
  <text x="300" y="390" font-family="Arial, sans-serif" font-size="18" font-weight="900" fill="#0f172a" text-anchor="middle">ĐIỀU HÒA DAIKIN INVERTER</text>
  <text x="300" y="415" font-family="Arial, sans-serif" font-size="12" font-weight="600" fill="#64748b" text-anchor="middle">Luồng Gió Thoải Mái Coanda • Gas R32</text>
</svg>""",

    "ac-daikin-remote.svg": """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 450" width="100%" height="100%">
  <rect width="600" height="450" fill="#f1f5f9"/>
  <!-- Remote Control -->
  <rect x="230" y="50" width="140" height="290" rx="14" fill="#ffffff" stroke="#94a3b8" stroke-width="3"/>
  <rect x="245" y="70" width="110" height="70" rx="4" fill="#0f172a"/>
  <text x="300" y="110" font-family="monospace" font-size="20" font-weight="bold" fill="#22c55e" text-anchor="middle">COOL 24°</text>
  <circle cx="300" cy="180" r="18" fill="#ef4444"/>
  <text x="300" y="185" font-family="Arial, sans-serif" font-size="11" font-weight="bold" fill="#fff" text-anchor="middle">ON/OFF</text>
  <text x="300" y="380" font-family="Arial, sans-serif" font-size="16" font-weight="bold" fill="#0f172a" text-anchor="middle">Điều khiển từ xa thông minh</text>
</svg>""",

    "ac-panasonic-nanoe.svg": """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 450" width="100%" height="100%">
  <rect width="600" height="450" fill="#f8fafc"/>
  <rect x="80" y="130" width="440" height="150" rx="12" fill="#ffffff" stroke="#cbd5e1" stroke-width="4"/>
  <text x="130" y="165" font-family="Arial, sans-serif" font-size="14" font-weight="900" fill="#1e293b" letter-spacing="2">Panasonic</text>
  <rect x="420" y="150" width="60" height="26" rx="4" fill="#0284c7"/>
  <text x="450" y="168" font-family="Arial, sans-serif" font-size="10" font-weight="bold" fill="#fff" text-anchor="middle">nanoe-X</text>
  <line x1="85" y1="230" x2="515" y2="230" stroke="#e2e8f0" stroke-width="2"/>
  <rect x="110" y="240" width="380" height="24" rx="4" fill="#f1f5f9" stroke="#94a3b8" stroke-width="1"/>
  <text x="300" y="390" font-family="Arial, sans-serif" font-size="18" font-weight="900" fill="#0f172a" text-anchor="middle">PANASONIC NANOE-X</text>
  <text x="300" y="415" font-family="Arial, sans-serif" font-size="12" font-weight="600" fill="#64748b" text-anchor="middle">Lọc Bụi Mịn PM2.5 • Diệt Khuẩn Suốt 24 Giờ</text>
</svg>""",

    "robot-dreame-l10s.svg": """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 450" width="100%" height="100%">
  <rect width="600" height="450" fill="#f8fafc"/>
  <!-- Circular Robot -->
  <circle cx="300" cy="200" r="130" fill="#ffffff" stroke="#cbd5e1" stroke-width="5"/>
  <!-- LDS Laser Turret -->
  <circle cx="300" cy="150" r="34" fill="#f1f5f9" stroke="#94a3b8" stroke-width="3"/>
  <circle cx="300" cy="150" r="16" fill="#ef4444"/>
  <!-- Buttons -->
  <circle cx="285" cy="235" r="8" fill="#94a3b8"/>
  <circle cx="315" cy="235" r="8" fill="#94a3b8"/>
  <!-- Front Bumper -->
  <path d="M190,140 A130,130 0 0,1 410,140" fill="none" stroke="#64748b" stroke-width="8"/>
  <text x="300" y="380" font-family="Arial, sans-serif" font-size="18" font-weight="900" fill="#0f172a" text-anchor="middle">ROBOT DREAME L10S ULTRA</text>
  <text x="300" y="405" font-family="Arial, sans-serif" font-size="12" font-weight="600" fill="#64748b" text-anchor="middle">Lực hút 5300Pa • Camera AI Tránh Vật Cản</text>
</svg>""",

    "robot-dreame-station.svg": """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 450" width="100%" height="100%">
  <rect width="600" height="450" fill="#f1f5f9"/>
  <!-- Station Base -->
  <rect x="210" y="70" width="180" height="270" rx="14" fill="#ffffff" stroke="#94a3b8" stroke-width="4"/>
  <rect x="230" y="90" width="60" height="70" rx="6" fill="#38bdf8" opacity="0.3"/>
  <text x="260" y="130" font-family="Arial, sans-serif" font-size="10" font-weight="bold" fill="#0284c7" text-anchor="middle">Nước sạch</text>
  <rect x="310" y="90" width="60" height="70" rx="6" fill="#64748b" opacity="0.3"/>
  <text x="340" y="130" font-family="Arial, sans-serif" font-size="10" font-weight="bold" fill="#334155" text-anchor="middle">Nước bẩn</text>
  <rect x="230" y="270" width="140" height="60" rx="6" fill="#0f172a"/>
  <text x="300" y="380" font-family="Arial, sans-serif" font-size="16" font-weight="bold" fill="#0f172a" text-anchor="middle">Trạm sạc đa năng tự giặt giẻ &amp; sấy khô</text>
</svg>""",

    "robot-roborock-qrevo.svg": """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 450" width="100%" height="100%">
  <rect width="600" height="450" fill="#f8fafc"/>
  <circle cx="300" cy="200" r="130" fill="#ffffff" stroke="#e2e8f0" stroke-width="5"/>
  <circle cx="300" cy="140" r="32" fill="#f8fafc" stroke="#dc2626" stroke-width="3"/>
  <text x="300" y="145" font-family="Arial, sans-serif" font-size="12" font-weight="bold" fill="#dc2626" text-anchor="middle">LDS</text>
  <!-- Dual spin mops preview -->
  <circle cx="260" cy="250" r="28" fill="#e0f2fe" stroke="#38bdf8" stroke-width="3"/>
  <circle cx="340" cy="250" r="28" fill="#e0f2fe" stroke="#38bdf8" stroke-width="3"/>
  <text x="300" y="380" font-family="Arial, sans-serif" font-size="18" font-weight="900" fill="#0f172a" text-anchor="middle">ROBOROCK Q REVO</text>
  <text x="300" y="405" font-family="Arial, sans-serif" font-size="12" font-weight="600" fill="#64748b" text-anchor="middle">Lau Xoay Kép 200 Vòng/Phút • Lực Hút 5500Pa</text>
</svg>""",

    "airpurifier-xiaomi-pro4.svg": """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 450" width="100%" height="100%">
  <rect width="600" height="450" fill="#f8fafc"/>
  <!-- Tower Body -->
  <rect x="230" y="50" width="140" height="300" rx="10" fill="#ffffff" stroke="#cbd5e1" stroke-width="4"/>
  <!-- Circular OLED Display -->
  <circle cx="300" cy="115" r="26" fill="#0f172a" stroke="#22c55e" stroke-width="3"/>
  <text x="300" y="120" font-family="monospace" font-size="13" font-weight="bold" fill="#22c55e" text-anchor="middle">012</text>
  <!-- Air grille holes -->
  <rect x="245" y="170" width="110" height="150" rx="6" fill="#f1f5f9"/>
  <line x1="255" y1="185" x2="345" y2="185" stroke="#94a3b8" stroke-width="2" stroke-dasharray="3,3"/>
  <line x1="255" y1="210" x2="345" y2="210" stroke="#94a3b8" stroke-width="2" stroke-dasharray="3,3"/>
  <line x1="255" y1="235" x2="345" y2="235" stroke="#94a3b8" stroke-width="2" stroke-dasharray="3,3"/>
  <line x1="255" y1="260" x2="345" y2="260" stroke="#94a3b8" stroke-width="2" stroke-dasharray="3,3"/>
  <line x1="255" y1="285" x2="345" y2="285" stroke="#94a3b8" stroke-width="2" stroke-dasharray="3,3"/>
  <!-- Text -->
  <text x="300" y="385" font-family="Arial, sans-serif" font-size="18" font-weight="900" fill="#0f172a" text-anchor="middle">XIAOMI SMART AIR PURIFIER 4 PRO</text>
  <text x="300" y="410" font-family="Arial, sans-serif" font-size="12" font-weight="600" fill="#64748b" text-anchor="middle">CADR 500m³/h • Màng lọc HEPA diệt 99.97% bụi mịn</text>
</svg>""",

    "airfryer-philips.svg": """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 450" width="100%" height="100%">
  <rect width="600" height="450" fill="#f1f5f9"/>
  <!-- Body -->
  <path d="M210,90 Q300,75 390,90 L400,320 Q300,340 200,320 Z" fill="#18181b" stroke="#3f3f46" stroke-width="4"/>
  <!-- Digital touch panel -->
  <rect x="245" y="105" width="110" height="50" rx="6" fill="#09090b" stroke="#f59e0b" stroke-width="2"/>
  <text x="300" y="136" font-family="monospace" font-size="18" font-weight="bold" fill="#f59e0b" text-anchor="middle">180°C</text>
  <!-- Basket Handle -->
  <rect x="235" y="195" width="130" height="100" rx="8" fill="#27272a"/>
  <rect x="285" y="220" width="30" height="60" rx="4" fill="#52525b"/>
  <!-- Text -->
  <text x="300" y="380" font-family="Arial, sans-serif" font-size="18" font-weight="900" fill="#0f172a" text-anchor="middle">NỒI CHIÊN KHÔNG DẦU PHILIPS 7.2L</text>
  <text x="300" y="405" font-family="Arial, sans-serif" font-size="12" font-weight="600" fill="#64748b" text-anchor="middle">Công nghệ Rapid Air giảm 90% dầu mỡ</text>
</svg>""",

    "smartlock-kaadas.svg": """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 450" width="100%" height="100%">
  <rect width="600" height="450" fill="#f8fafc"/>
  <!-- Tall Lock Body -->
  <rect x="245" y="40" width="110" height="330" rx="14" fill="#18181b" stroke="#27272a" stroke-width="4"/>
  <!-- Face ID Camera Bar -->
  <rect x="260" y="60" width="80" height="40" rx="6" fill="#09090b" stroke="#38bdf8" stroke-width="2"/>
  <circle cx="285" cy="80" r="7" fill="#38bdf8"/>
  <circle cx="315" cy="80" r="7" fill="#64748b"/>
  <!-- Touch Keypad -->
  <circle cx="275" cy="130" r="7" fill="#52525b"/>
  <circle cx="300" cy="130" r="7" fill="#52525b"/>
  <circle cx="325" cy="130" r="7" fill="#52525b"/>
  <circle cx="275" cy="155" r="7" fill="#52525b"/>
  <circle cx="300" cy="155" r="7" fill="#52525b"/>
  <circle cx="325" cy="155" r="7" fill="#52525b"/>
  <!-- Handle -->
  <rect x="220" y="210" width="115" height="28" rx="6" fill="#3f3f46"/>
  <circle cx="240" cy="224" r="8" fill="#22c55e"/>
  <!-- Text -->
  <text x="300" y="400" font-family="Arial, sans-serif" font-size="18" font-weight="900" fill="#0f172a" text-anchor="middle">KHÓA THÔNG MINH KAADAS K20-F</text>
  <text x="300" y="425" font-family="Arial, sans-serif" font-size="12" font-weight="600" fill="#64748b" text-anchor="middle">Mở khóa 3D Face ID • Vân tay FPC Thụy Điển</text>
</svg>""",

    "camera-ezviz-c8w.svg": """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 450" width="100%" height="100%">
  <rect width="600" height="450" fill="#f8fafc"/>
  <!-- Wall Mount Bracket -->
  <rect x="270" y="40" width="60" height="50" rx="4" fill="#94a3b8"/>
  <path d="M290,90 L290,130 L310,130 L310,90 Z" fill="#64748b"/>
  <!-- Spherical Camera Body -->
  <circle cx="300" cy="210" r="90" fill="#ffffff" stroke="#cbd5e1" stroke-width="4"/>
  <!-- Rotating Lens Sphere -->
  <circle cx="300" cy="210" r="60" fill="#0f172a"/>
  <circle cx="300" cy="210" r="28" fill="#1e293b" stroke="#38bdf8" stroke-width="3"/>
  <circle cx="300" cy="210" r="12" fill="#0284c7"/>
  <!-- Dual Antennas -->
  <line x1="220" y1="130" x2="250" y2="180" stroke="#64748b" stroke-width="6" stroke-linecap="round"/>
  <line x1="380" y1="130" x2="350" y2="180" stroke="#64748b" stroke-width="6" stroke-linecap="round"/>
  <!-- Text -->
  <text x="300" y="380" font-family="Arial, sans-serif" font-size="18" font-weight="900" fill="#0f172a" text-anchor="middle">CAMERA EZVIZ C8W 4MP 2K+</text>
  <text x="300" y="405" font-family="Arial, sans-serif" font-size="12" font-weight="600" fill="#64748b" text-anchor="middle">Xoay 360 độ Ngoài Trời • Ban Đêm Có Màu • Còi Báo Động</text>
</svg>"""
}

target_dirs = [
    os.path.join("uploads", "products"),
    os.path.join("src", "main", "resources", "static", "uploads", "products"),
    os.path.join("target", "classes", "static", "uploads", "products")
]

for d in target_dirs:
    os.makedirs(d, exist_ok=True)

for fname, content in images.items():
    for d in target_dirs:
        p = os.path.join(d, fname)
        with open(p, "w", encoding="utf-8") as f:
            f.write(content.strip())

print(f"Generated {len(images)} images in {len(target_dirs)} directories successfully.")
