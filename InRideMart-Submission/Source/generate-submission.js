const fs = require("fs");
const path = require("path");

const projectRoot = path.resolve(__dirname, "..", "..");
const packageRoot = path.resolve(__dirname, "..");
const screenshotsDir = path.join(packageRoot, "Screenshots");
const htmlDir = path.join(packageRoot, "Html");
const sharp = require(path.join(projectRoot, "frontend", "node_modules", "sharp"));

const colors = {
  ink: "#172033",
  teal: "#007F82",
  coral: "#EE6C4D",
  sky: "#EDF6F7",
  cream: "#FFFDFC",
  slate: "#536276",
  line: "#D9E3E6",
  gold: "#F2B84B",
  green: "#227C5D"
};

function ensure(directory) {
  fs.mkdirSync(directory, { recursive: true });
}

function write(relativePath, content) {
  const output = path.join(packageRoot, relativePath);
  ensure(path.dirname(output));
  fs.writeFileSync(output, content, "utf8");
}

function escapeXml(value) {
  return String(value)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/\"/g, "&quot;");
}

function svgText(value) {
  return escapeXml(value).replace(/\n/g, "&#10;");
}

function page(content, number, total, label = "OpenAI Build Week 2026") {
  return `<section class="page">
    ${content}
    <footer class="footer"><span>InRideMart | ${label}</span><span>Page ${number} of ${total}</span></footer>
  </section>`;
}

function documentShell(title, pages, options = {}) {
  const orientation = options.landscape ? "landscape" : "portrait";
  const pageSize = options.landscape ? "A4 landscape" : "A4";
  return `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>${title}</title>
  <style>
    @page { size: ${pageSize}; margin: 0; }
    :root { color-scheme: light; }
    * { box-sizing: border-box; }
    html, body { margin: 0; padding: 0; background: #dfe8ea; color: ${colors.ink}; font-family: Arial, "Segoe UI", sans-serif; }
    body.${orientation} .page { width: ${options.landscape ? "297mm" : "210mm"}; min-height: ${options.landscape ? "210mm" : "297mm"}; }
    .page { position: relative; overflow: hidden; background: ${colors.cream}; padding: 14mm 15mm 18mm; page-break-after: always; }
    .page:last-child { page-break-after: auto; }
    .eyebrow { color: ${colors.teal}; font-size: 9pt; font-weight: 800; letter-spacing: 1.3px; text-transform: uppercase; margin-bottom: 5px; }
    h1, h2, h3, p { margin-top: 0; }
    h1 { font-size: 30pt; line-height: 1.08; letter-spacing: 0; margin-bottom: 12px; }
    h2 { font-size: 18pt; line-height: 1.2; letter-spacing: 0; margin-bottom: 10px; }
    h3 { font-size: 11.5pt; line-height: 1.25; letter-spacing: 0; margin-bottom: 6px; }
    p, li, td, th { font-size: 9.5pt; line-height: 1.52; }
    p { margin-bottom: 9px; }
    ul, ol { margin: 0 0 12px 19px; padding: 0; }
    li { margin: 3px 0; }
    .lede { color: ${colors.slate}; font-size: 13pt; line-height: 1.45; max-width: 158mm; }
    .muted { color: ${colors.slate}; }
    .tiny { color: ${colors.slate}; font-size: 7.8pt; line-height: 1.4; }
    .rule { height: 1px; background: ${colors.line}; margin: 12px 0; }
    .accent-line { width: 48px; height: 5px; border-radius: 3px; background: ${colors.coral}; margin: 11px 0 15px; }
    .hero-grid { display: grid; grid-template-columns: 1.4fr 0.8fr; gap: 14mm; align-items: center; min-height: 190mm; }
    .hero-mark { position: relative; min-height: 145mm; border-radius: 18px; background: linear-gradient(145deg, ${colors.ink} 0%, #273a52 58%, ${colors.teal} 160%); padding: 9mm; color: white; overflow: hidden; }
    .hero-mark:before { content: ""; position: absolute; width: 140mm; height: 140mm; right: -72mm; bottom: -64mm; border: 20px solid rgba(255,255,255,.12); border-radius: 50%; }
    .hero-mark:after { content: ""; position: absolute; width: 62mm; height: 62mm; top: -26mm; left: -20mm; border: 13px solid rgba(238,108,77,.78); border-radius: 50%; }
    .brand { position: relative; font-size: 15pt; font-weight: 800; letter-spacing: 0; margin: 0; white-space: nowrap; }
    .brand-sub { position: relative; color: #dce9ec; font-size: 10pt; max-width: 48mm; margin-top: 9px; }
    .ride-chip { position: relative; display: inline-flex; align-items: center; gap: 7px; border: 1px solid rgba(255,255,255,.28); border-radius: 999px; padding: 7px 9px; margin-top: 18mm; font-size: 7.5pt; }
    .dot { width: 8px; height: 8px; border-radius: 50%; background: ${colors.coral}; display: inline-block; }
    .stat-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 7px; margin-top: 12px; }
    .stat { background: ${colors.sky}; border: 1px solid ${colors.line}; padding: 10px; border-radius: 8px; }
    .stat strong { display: block; color: ${colors.teal}; font-size: 18pt; }
    .stat span { color: ${colors.slate}; font-size: 8pt; line-height: 1.25; }
    .grid-2 { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; }
    .grid-3 { display: grid; grid-template-columns: repeat(3, 1fr); gap: 9px; }
    .grid-4 { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; }
    .card { border: 1px solid ${colors.line}; border-radius: 8px; background: white; padding: 11px; }
    .card.highlight { background: ${colors.sky}; border-color: #b9dfe0; }
    .card.coral { background: #fff3ef; border-color: #f3c3b6; }
    .card h3 { margin-bottom: 5px; }
    .card p { color: ${colors.slate}; font-size: 8.6pt; margin: 0; line-height: 1.42; }
    .icon { width: 28px; height: 28px; border-radius: 7px; display: inline-flex; align-items: center; justify-content: center; background: ${colors.teal}; color: white; font-weight: 800; font-size: 11pt; margin-bottom: 7px; }
    .icon.coral { background: ${colors.coral}; }
    .icon.gold { background: ${colors.gold}; color: ${colors.ink}; }
    .callout { padding: 10px 12px; border-left: 4px solid ${colors.teal}; background: ${colors.sky}; color: #29444b; margin: 10px 0; font-size: 9pt; line-height: 1.45; }
    .callout.warn { border-left-color: ${colors.coral}; background: #fff3ef; color: #703425; }
    .pill { display: inline-block; background: #e1f0ef; color: #126668; border-radius: 999px; padding: 3px 7px; margin: 0 4px 5px 0; font-size: 7.7pt; font-weight: 700; }
    .pill.coral { background: #fbe1d8; color: #a23b26; }
    table { width: 100%; border-collapse: collapse; margin: 7px 0 11px; }
    th { text-align: left; color: ${colors.teal}; font-size: 8.2pt; text-transform: uppercase; letter-spacing: .6px; padding: 7px 6px; border-bottom: 1px solid ${colors.line}; }
    td { vertical-align: top; padding: 7px 6px; border-bottom: 1px solid #e8eff0; font-size: 8.5pt; }
    code { display: inline-block; background: #eff3f4; border: 1px solid ${colors.line}; color: #263b4b; border-radius: 4px; padding: 1px 4px; font-family: Consolas, monospace; font-size: 7.7pt; }
    pre { margin: 7px 0 10px; padding: 9px 10px; border-radius: 7px; background: ${colors.ink}; color: #ebf6f8; font: 7.7pt/1.5 Consolas, monospace; white-space: pre-wrap; }
    .toc { columns: 2; column-gap: 12mm; margin-top: 12px; }
    .toc div { break-inside: avoid; display: flex; justify-content: space-between; gap: 8px; border-bottom: 1px dotted #b4c5c9; padding: 5px 0; font-size: 9pt; }
    .toc span:last-child { color: ${colors.teal}; font-weight: 800; }
    .footer { position: absolute; bottom: 7mm; left: 15mm; right: 15mm; display: flex; justify-content: space-between; color: #6d7b8b; font-size: 7pt; border-top: 1px solid ${colors.line}; padding-top: 3mm; }
    .flow { display: flex; align-items: center; gap: 5px; flex-wrap: wrap; margin: 8px 0; }
    .flow .node { padding: 7px 9px; border-radius: 6px; border: 1px solid ${colors.line}; background: white; font-size: 8.2pt; font-weight: 700; }
    .flow .node.primary { background: ${colors.teal}; color: white; border-color: ${colors.teal}; }
    .flow .node.coral { background: ${colors.coral}; color: white; border-color: ${colors.coral}; }
    .flow .arrow { color: ${colors.coral}; font-size: 14pt; font-weight: 800; }
    .step { display: grid; grid-template-columns: 13mm 1fr; gap: 8px; align-items: start; margin-bottom: 10px; }
    .step-num { width: 11mm; height: 11mm; display: flex; align-items: center; justify-content: center; background: ${colors.teal}; border-radius: 50%; color: white; font-weight: 800; font-size: 10pt; }
    .step h3 { margin: 1px 0 3px; }
    .step p { color: ${colors.slate}; margin: 0; font-size: 8.8pt; }
    .screen-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 9px; }
    .screen-card { border: 1px solid ${colors.line}; background: white; border-radius: 8px; overflow: hidden; }
    .screen-card img { display: block; width: 100%; aspect-ratio: 16/9; object-fit: cover; border-bottom: 1px solid ${colors.line}; }
    .screen-card div { padding: 8px 9px 9px; }
    .screen-card h3 { font-size: 10pt; margin: 0 0 3px; }
    .screen-card p { margin: 0; color: ${colors.slate}; font-size: 7.8pt; line-height: 1.35; }
    .mini-note { color: #a23b26; font-size: 7pt; font-weight: 800; letter-spacing: .4px; text-transform: uppercase; }
    .stack-card { display: grid; grid-template-columns: 28px 1fr; gap: 8px; padding: 9px; min-height: 75px; }
    .stack-card .mark { width: 28px; height: 28px; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: white; background: ${colors.teal}; font-size: 8pt; font-weight: 800; }
    .stack-card:nth-child(3n+2) .mark { background: ${colors.coral}; }
    .stack-card:nth-child(3n) .mark { background: ${colors.gold}; color: ${colors.ink}; }
    .stack-card h3 { font-size: 9pt; margin: 0 0 2px; }
    .stack-card p { color: ${colors.slate}; font-size: 7.5pt; line-height: 1.35; margin: 0; }
    .quote { color: ${colors.teal}; font-size: 18pt; font-weight: 800; line-height: .8; }
    .architecture { margin: 8mm auto 0; max-width: 257mm; }
    .arch-top { display: flex; justify-content: center; gap: 6px; align-items: center; }
    .arch-box { text-align: center; min-width: 40mm; padding: 9px 10px; border: 1px solid ${colors.line}; border-radius: 8px; background: white; font-size: 8.6pt; font-weight: 800; }
    .arch-box.primary { background: ${colors.teal}; border-color: ${colors.teal}; color: white; }
    .arch-box.dark { background: ${colors.ink}; border-color: ${colors.ink}; color: white; }
    .arch-box.coral { background: ${colors.coral}; border-color: ${colors.coral}; color: white; }
    .arch-arrow { text-align: center; color: ${colors.coral}; font-size: 17pt; font-weight: 800; margin: 4px; }
    .service-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 5px; }
    .service-grid .arch-box { min-width: 0; padding: 9px 4px; font-size: 7.5pt; }
    .data-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 7px; max-width: 180mm; margin: 0 auto; }
    .connector-row { height: 12mm; position: relative; }
    .connector-row:before { content: ""; position: absolute; top: 0; left: 50%; height: 100%; border-left: 2px solid ${colors.coral}; }
    .connector-row:after { content: ""; position: absolute; bottom: -1px; left: calc(50% - 4px); border-left: 5px solid transparent; border-right: 5px solid transparent; border-top: 7px solid ${colors.coral}; }
    .diagram-legend { display: flex; justify-content: center; gap: 16px; margin-top: 9mm; color: ${colors.slate}; font-size: 8pt; }
    .legend-dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; margin-right: 4px; background: ${colors.teal}; }
    .legend-dot.coral { background: ${colors.coral}; }
    .roadmap { display: grid; grid-template-columns: 30mm 1fr; gap: 8px; padding: 8px 0; border-bottom: 1px solid ${colors.line}; }
    .roadmap strong { color: ${colors.teal}; font-size: 8.5pt; text-transform: uppercase; letter-spacing: .6px; }
    .roadmap div { color: ${colors.slate}; font-size: 8.8pt; line-height: 1.45; }
  </style>
</head>
<body class="${orientation}">
${pages.join("\n")}
</body>
</html>`;
}

const screenshotSpecs = [
  ["Home", "Home Screen", "Ride context, cab-available products, recommendations, and bundles."],
  ["Catalog", "Product Catalog", "Browse and search the current cab-available catalog."],
  ["AI-Chat", "AI Shopping Assistant", "Conversational shopping with contextual recommendations."],
  ["Recommendations", "Smart Recommendations", "Explainable products and optional travel bundles."],
  ["Cart", "Cart", "Review selected products, quantities, prices, and total."],
  ["Checkout", "Checkout", "Authenticated checkout and hand-off to mock payment."],
  ["Payment", "Mock Payment", "UPI, card, debit-card, or wallet payment confirmation."],
  ["OrderTracking", "Order Tracking", "Placed, confirmed, handed over, and completed status timeline."],
  ["Translation", "Multilingual Translation", "Passenger-to-driver and driver-to-passenger translation view."]
];

async function createPlaceholder(fileName, title, caption) {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="1600" height="900" viewBox="0 0 1600 900">
  <rect width="1600" height="900" fill="${colors.sky}"/>
  <rect x="80" y="70" width="1440" height="760" rx="28" fill="#ffffff" stroke="${colors.line}" stroke-width="4"/>
  <rect x="80" y="70" width="1440" height="102" rx="28" fill="${colors.ink}"/>
  <rect x="80" y="146" width="1440" height="26" fill="${colors.ink}"/>
  <circle cx="140" cy="121" r="17" fill="${colors.coral}"/>
  <text x="180" y="132" font-family="Arial, Segoe UI, sans-serif" font-size="39" font-weight="700" fill="#ffffff">InRideMart</text>
  <rect x="122" y="221" width="310" height="505" rx="18" fill="${colors.ink}"/>
  <rect x="468" y="221" width="955" height="113" rx="18" fill="${colors.teal}" opacity=".94"/>
  <rect x="468" y="362" width="292" height="364" rx="18" fill="${colors.sky}" stroke="${colors.line}" stroke-width="3"/>
  <rect x="790" y="362" width="292" height="364" rx="18" fill="#fff4ef" stroke="#f2c1b4" stroke-width="3"/>
  <rect x="1112" y="362" width="311" height="364" rx="18" fill="#eef7f1" stroke="#bdddcf" stroke-width="3"/>
  <rect x="155" y="260" width="230" height="20" rx="10" fill="#ffffff" opacity=".83"/>
  <rect x="155" y="306" width="178" height="15" rx="7" fill="#ffffff" opacity=".35"/>
  <rect x="155" y="352" width="206" height="15" rx="7" fill="#ffffff" opacity=".35"/>
  <rect x="155" y="398" width="152" height="15" rx="7" fill="#ffffff" opacity=".35"/>
  <rect x="155" y="582" width="213" height="76" rx="14" fill="${colors.coral}"/>
  <text x="510" y="272" font-family="Arial, Segoe UI, sans-serif" font-size="34" font-weight="700" fill="#ffffff">${svgText(title)}</text>
  <text x="510" y="312" font-family="Arial, Segoe UI, sans-serif" font-size="22" fill="#d9f0f0">${svgText(caption)}</text>
  <text x="614" y="533" text-anchor="middle" font-family="Arial, Segoe UI, sans-serif" font-size="31" font-weight="700" fill="${colors.teal}">SCREEN</text>
  <text x="936" y="533" text-anchor="middle" font-family="Arial, Segoe UI, sans-serif" font-size="31" font-weight="700" fill="${colors.coral}">PREVIEW</text>
  <text x="1268" y="533" text-anchor="middle" font-family="Arial, Segoe UI, sans-serif" font-size="31" font-weight="700" fill="${colors.green}">STATE</text>
  <text x="800" y="785" text-anchor="middle" font-family="Arial, Segoe UI, sans-serif" font-size="30" font-weight="800" fill="${colors.ink}">SCREENSHOT PLACEHOLDER - REPLACE WITH ACTUAL APP CAPTURE</text>
  <text x="800" y="818" text-anchor="middle" font-family="Arial, Segoe UI, sans-serif" font-size="20" fill="${colors.slate}">This layout is intentionally illustrative only. Capture the live application before submitting.</text>
</svg>`;
  await sharp(Buffer.from(svg)).png().toFile(path.join(screenshotsDir, `${fileName}.png`));
}

function generateReadmePdf() {
  const total = 4;
  const pages = [
    page(`<div class="hero-grid">
      <div>
        <div class="eyebrow">OpenAI Build Week Hackathon Submission</div>
        <h1>InRideMart<br/>AI-Powered In-Ride Shopping Concierge</h1>
        <div class="accent-line"></div>
        <p class="lede">A full-stack commerce experience that helps passengers discover useful, cab-available convenience products during a ride, with an AI assistant that turns travel context into explainable suggestions.</p>
        <div class="callout">Built as an existing Java Spring Boot microservice monorepo with a Next.js frontend. The project reuses service ownership across authentication, catalog, cart, checkout, payment, and AI orchestration.</div>
        <h2>Executive Summary</h2>
        <p>InRideMart addresses the small but time-sensitive needs that surface in transit: a dead phone, a long airport journey, hunger, rain, or a forgotten essential. It keeps discovery and purchase close to the passenger while preserving the Catalog service as the product source of truth.</p>
        <div class="grid-3">
          <div class="card"><div class="icon">1</div><h3>Discover</h3><p>Browse the active catalog and assigned demo cab inventory.</p></div>
          <div class="card"><div class="icon coral">2</div><h3>Decide</h3><p>Ask the assistant for relevant products and optional bundles.</p></div>
          <div class="card"><div class="icon gold">3</div><h3>Complete</h3><p>Use cart, checkout, mock payment, and order tracking.</p></div>
        </div>
        <h3 style="margin-top:12px">Contents</h3>
        <div class="toc"><div><span>Executive summary</span><span>1</span></div><div><span>Problem, solution, features</span><span>2</span></div><div><span>Architecture and operations</span><span>3</span></div><div><span>AI capabilities and roadmap</span><span>4</span></div></div>
      </div>
      <aside class="hero-mark">
        <p class="brand">InRideMart</p>
        <p class="brand-sub">A calm, practical shopping assistant for the time between pickup and drop-off.</p>
        <div class="ride-chip"><span class="dot"></span> Build Week MVP</div>
      </aside>
    </div>`, 1, total),
    page(`<div class="eyebrow">Overview</div><h2>Problem, Solution, and Features</h2><div class="accent-line"></div>
      <div class="grid-2">
        <div class="card highlight"><h3>Problem Statement</h3><p>Passengers often realize they need a charger, drink, snack, travel comfort item, or rain essential only after a ride has started. Traditional quick-commerce services are not optimized for the short time window, current journey context, or inventory that is immediately accessible in the vehicle.</p></div>
        <div class="card coral"><h3>Solution Overview</h3><p>InRideMart brings a catalog-led shopping flow into the ride. A conversational AI layer understands intent and context, then recommends only products returned by the existing catalog and available in the assigned demo cab inventory.</p></div>
      </div>
      <h2 style="margin-top:15px">Implemented MVP Capabilities</h2>
      <div class="grid-3">
        <div class="card"><div class="icon">AI</div><h3>Conversational Shopping</h3><p>Natural requests, clarifying questions, budget-aware suggestions, and catalog-grounded explanations.</p></div>
        <div class="card"><div class="icon coral">CB</div><h3>Travel Bundles</h3><p>Contextual bundle suggestions such as airport, charging, rainy-day, and road-trip essentials.</p></div>
        <div class="card"><div class="icon gold">CI</div><h3>Cab Inventory View</h3><p>Frontend filters the existing catalog to the assigned demo cab inventory. It is not a live driver inventory integration.</p></div>
        <div class="card"><div class="icon">CT</div><h3>Cart and Checkout</h3><p>Authenticated cart ownership, product validation, checkout, and order creation through dedicated services.</p></div>
        <div class="card"><div class="icon coral">PM</div><h3>Mock Payment</h3><p>UPI, credit card, debit card, and wallet choices with a production-like successful-payment experience.</p></div>
        <div class="card"><div class="icon gold">TR</div><h3>Order Tracking</h3><p>Order progression from placed to confirmed, handed over, and completed.</p></div>
      </div>
      <div class="callout warn"><strong>Scope note:</strong> All product availability, prices, and names come from the Catalog service. AI does not invent products, prices, stock, or payment outcomes.</div>`, 2, total),
    page(`<div class="eyebrow">Architecture and Operations</div><h2>How the Platform Runs</h2><div class="accent-line"></div>
      <p>Next.js provides the mobile-first web experience and a same-origin API proxy. The backend uses independently owned Spring Boot services; PostgreSQL, Redis, and Kafka are started through Docker Compose for local development.</p>
      <div class="flow"><span class="node primary">Next.js Frontend :3000</span><span class="arrow">-></span><span class="node coral">API Proxy</span><span class="arrow">-></span><span class="node">Spring Boot Services</span><span class="arrow">-></span><span class="node">PostgreSQL / Redis / Kafka</span></div>
      <div class="grid-2">
        <div class="card"><h3>Service Ports</h3><table><tr><th>Service</th><th>Port</th></tr><tr><td>Auth</td><td>8081</td></tr><tr><td>Customer</td><td>8082</td></tr><tr><td>Catalog</td><td>8083</td></tr><tr><td>Cart</td><td>8084</td></tr><tr><td>Order</td><td>8085</td></tr><tr><td>AI</td><td>8086</td></tr><tr><td>Payment</td><td>8087</td></tr></table></div>
        <div class="card"><h3>API Surface</h3><p><code>/api/v1/auth</code> register, login, current user</p><p><code>/api/v1/customers</code> customer profiles</p><p><code>/api/v1/catalog/products</code> browse and search</p><p><code>/api/v1/carts/me</code> cart, items, checkout</p><p><code>/api/v1/orders</code> orders and tracking</p><p><code>/api/v1/payments</code> mock payment and status</p><p><code>/api/v1/ai</code> chat and translation</p></div>
      </div>
      <h2 style="margin-top:12px">Local Setup</h2>
      <pre>git clone &lt;REPLACE-WITH-REPOSITORY-URL&gt;
cd InRideMart
$env:OPENAI_API_KEY = "&lt;your-key&gt;"   # optional; fallback remains available
.\\start-inridemart.ps1</pre>
      <p class="tiny">The start script sets local development variables, starts Docker Compose, opens service processes, and runs the frontend. PostgreSQL is exposed on host port 5433; Redis uses 6379; Kafka uses 9092.</p>`, 3, total),
    page(`<div class="eyebrow">AI and Roadmap</div><h2>AI Capabilities and Next Steps</h2><div class="accent-line"></div>
      <div class="grid-2">
        <div class="card highlight"><h3>How GPT-5.6 Is Used</h3><p>The dedicated AI service uses the OpenAI Responses API for conversational reasoning, intent detection, follow-up questions, and multilingual translation. The model is configured through environment variables; no secret is embedded in source.</p><p>Known shopping intents are matched against catalog data, with context including budget, destination, weather, journey duration, purpose, and available cab inventory. GPT is never asked to create product facts outside that catalog.</p></div>
        <div class="card"><h3>Resilient Behavior</h3><p>When an OpenAI key is unavailable or a live call fails, the AI service uses deterministic, catalog-grounded handling and makes that fallback explicit. Empty or ambiguous requests prompt for clarification rather than producing a generic product list.</p><p>The multilingual assistant uses GPT when configured and local phrase support only when live translation is unavailable.</p></div>
      </div>
      <h2 style="margin-top:14px">Future Roadmap</h2>
      <div class="roadmap"><strong>Inventory</strong><div>Live driver and cab inventory synchronization, replenishment signals, and availability updates.</div></div>
      <div class="roadmap"><strong>Commerce</strong><div>Real payment-gateway integration, refunds, receipt delivery, loyalty rewards, and richer fulfillment states.</div></div>
      <div class="roadmap"><strong>Mobility</strong><div>Ride booking integration, destination signals, driver workflows, and a native mobile client using the same backend APIs.</div></div>
      <div class="roadmap"><strong>AI</strong><div>Voice interaction, stronger preference memory with consent, and predictive recommendations based on opt-in trip history.</div></div>
      <h2 style="margin-top:15px">OpenAPI Documentation</h2>
      <table><tr><th>Service</th><th>Swagger UI</th></tr><tr><td>Auth</td><td><code>http://localhost:8081/swagger-ui.html</code></td></tr><tr><td>Customer</td><td><code>http://localhost:8082/swagger-ui.html</code></td></tr><tr><td>Catalog</td><td><code>http://localhost:8083/swagger-ui.html</code></td></tr><tr><td>Order</td><td><code>http://localhost:8085/swagger-ui.html</code></td></tr><tr><td>AI</td><td><code>http://localhost:8086/swagger-ui.html</code></td></tr></table>
      <p class="tiny">Cart and Payment APIs are available through their service endpoints but do not currently expose a Springdoc Swagger UI in the repository configuration.</p>`, 4, total)
  ];
  write("Html/README.html", documentShell("InRideMart Submission Readme", pages));
}

function generateArchitecturePdf() {
  const pages = [page(`<div class="eyebrow">System Architecture</div><h1 style="font-size:27pt">InRideMart Platform Architecture</h1><p class="lede">A catalog-led, service-owned commerce flow with a dedicated AI orchestration boundary.</p><div class="accent-line"></div>
    <div class="architecture">
      <div class="arch-top"><div class="arch-box dark">Passenger Web App<br/><span class="tiny" style="color:#d7e2e5">Next.js + React + TypeScript</span></div></div>
      <div class="connector-row"></div>
      <div class="arch-top"><div class="arch-box coral">Same-Origin API Proxy<br/><span class="tiny" style="color:#fff0e9">Authorization header forwarding</span></div></div>
      <div class="connector-row"></div>
      <div class="service-grid">
        <div class="arch-box primary">Auth<br/><span class="tiny" style="color:#d7f4f4">JWT issuance</span></div>
        <div class="arch-box primary">Customer<br/><span class="tiny" style="color:#d7f4f4">Profiles</span></div>
        <div class="arch-box primary">Catalog<br/><span class="tiny" style="color:#d7f4f4">Products</span></div>
        <div class="arch-box primary">Cart<br/><span class="tiny" style="color:#d7f4f4">Cart logic</span></div>
        <div class="arch-box primary">Order<br/><span class="tiny" style="color:#d7f4f4">Checkout</span></div>
        <div class="arch-box primary">Payment<br/><span class="tiny" style="color:#d7f4f4">Mock payments</span></div>
        <div class="arch-box primary">AI<br/><span class="tiny" style="color:#d7f4f4">Chat + translate</span></div>
      </div>
      <div class="connector-row"></div>
      <div class="data-grid">
        <div class="arch-box">PostgreSQL<br/><span class="tiny">Service data, Flyway migrations</span></div>
        <div class="arch-box">Redis<br/><span class="tiny">Local infrastructure component</span></div>
        <div class="arch-box">Kafka<br/><span class="tiny">Local infrastructure component</span></div>
      </div>
      <div class="flow" style="justify-content:center; margin-top:8mm"><span class="node primary">AI Service</span><span class="arrow">-></span><span class="node coral">OpenAI Responses API (GPT-5.6)</span></div>
    </div>
    <div class="diagram-legend"><span><i class="legend-dot"></i>Backend service boundary</span><span><i class="legend-dot coral"></i>Request and orchestration path</span></div>
    <div class="grid-3" style="margin-top:9mm"><div class="card"><h3>Authentication</h3><p>Auth issues JWTs. Protected services validate the same issuer and secret, while the frontend proxy preserves the Bearer header.</p></div><div class="card"><h3>Commerce Ownership</h3><p>Catalog owns products; Cart owns cart operations; Order owns checkout and tracking; Payment owns mock payment state.</p></div><div class="card"><h3>AI Grounding</h3><p>AI interprets requests and selects only Catalog-returned products. It does not become a competing product database.</p></div></div>`, 1, 1, "Architecture Overview")];
  write("Html/Architecture-Diagram.html", documentShell("InRideMart Architecture", pages, { landscape: true }));
}

function featureCard(image, title, description, note = "") {
  return `<article class="screen-card"><img src="../Screenshots/${image}.png" alt="${title} placeholder"/><div><h3>${title}</h3><p>${description}</p>${note ? `<div class="mini-note">${note}</div>` : ""}</div></article>`;
}

function generateFeaturePdf() {
  const total = 3;
  const pages = [
    page(`<div class="eyebrow">Product Experience</div><h1 style="font-size:27pt">Feature Overview</h1><p class="lede">A mobile-first in-ride shopping flow designed around the passenger's immediate travel needs.</p><div class="callout warn"><strong>Screenshot package:</strong> The visual panels in this PDF are intentional placeholders, not fabricated application screenshots. Replace each PNG in <code>Screenshots/</code> with a current capture of the running product before final Devpost upload.</div><div class="screen-grid">
      ${featureCard("Home", "Landing and Home", "The landing experience introduces InRideMart to guests. Once authenticated, Home surfaces ride context, cab-available products, personalized ideas, travel essentials, and bundles.")}
      ${featureCard("Catalog", "Product Catalog", "Guests can browse the catalog, while authenticated passengers can move relevant items into cart. Catalog remains the source of truth for product names, prices, and availability.")}
      ${featureCard("AI-Chat", "AI Shopping Assistant", "A conversational chat experience handles greetings, asks concise follow-up questions for ambiguous needs, and factors in travel context before recommendations.")}
      ${featureCard("Recommendations", "Smart Recommendations", "Recommendation cards explain why a catalog item fits the journey and offer optional bundles. Recommendations remain grounded in catalog and cab-available data.")}
    </div>`, 1, total),
    page(`<div class="eyebrow">Purchase Journey</div><h2>From Need to Confirmation</h2><div class="accent-line"></div><div class="screen-grid">
      ${featureCard("Cart", "Cart", "The cart view presents item quantities, per-item prices, subtotal, and grand total before checkout. Authentication protects purchase actions.")}
      ${featureCard("Checkout", "Checkout", "Checkout converts the authenticated cart into an order through the Order service and clears the cart after a successful result.")}
      ${featureCard("Payment", "Mock Payment", "The mock payment UI offers UPI, credit card, debit card, and wallet methods. A successful payment becomes PAID and drives order confirmation.")}
      ${featureCard("OrderTracking", "Order Tracking", "Passengers can retrieve the order and observe the simplified tracking lifecycle: PLACED, CONFIRMED, HANDED_OVER, and COMPLETED.")}
    </div><div class="flow" style="margin-top:14px"><span class="node">Cart</span><span class="arrow">-></span><span class="node">Checkout</span><span class="arrow">-></span><span class="node coral">Mock Payment</span><span class="arrow">-></span><span class="node primary">Confirmed Order</span></div>`, 2, total),
    page(`<div class="eyebrow">Travel Assistance</div><h2>Context, Inventory, and Translation</h2><div class="accent-line"></div><div class="screen-grid">
      ${featureCard("Catalog", "Assigned Cab Inventory", "The frontend uses an assigned demo cab inventory allow-list over the existing catalog. This feature demonstrates availability-aware shopping; live driver inventory synchronization is future scope.", "Demo inventory - not live driver inventory")}
      ${featureCard("Translation", "Multilingual Travel Assistant", "The AI service supports passenger-driver translation and detects language automatically. When configured, GPT-5.6 is used for live translation; a local fallback is shown only when the live model is unavailable.")}
      ${featureCard("Recommendations", "Ride Context", "Destination, journey duration, weather, and travel purpose can be captured on Home and supplied with AI requests to make recommendations more relevant.")}
      ${featureCard("AI-Chat", "Contextual Bundles", "Travel Kit, Charging Kit, Road Trip Pack, Rainy Day Kit, and Healthy Snacks Pack are presented only when their catalog items are available and relevant.")}
    </div><div class="callout">Accessibility and responsiveness are part of the frontend baseline: mobile-first layouts, loading and error states, protected routes, keyboard-friendly controls, and readable contrast.</div>`, 3, total)
  ];
  write("Html/Feature-Overview.html", documentShell("InRideMart Feature Overview", pages));
}

function generateDemoGuidePdf() {
  const total = 2;
  const pages = [
    page(`<div class="eyebrow">Judging Flow</div><h1 style="font-size:26pt">Demo Guide</h1><p class="lede">A repeatable eight-step path for judging the working in-ride commerce experience.</p><div class="accent-line"></div>
      <div class="step"><div class="step-num">1</div><div><h3>Register</h3><p>Open the landing page and create a fresh customer account. <strong>Expected:</strong> registration succeeds and the user is signed in or can sign in with the same credentials.</p></div></div>
      <div class="step"><div class="step-num">2</div><div><h3>Login</h3><p>Sign in using the Auth service flow. <strong>Expected:</strong> a JWT-backed session enables protected pages and requests.</p></div></div>
      <div class="step"><div class="step-num">3</div><div><h3>Browse Catalog</h3><p>Visit Catalog and search familiar travel items such as charger, coffee, snacks, water, or headphones. <strong>Expected:</strong> results come from Catalog and reflect the demo cab inventory view where shown.</p></div></div>
      <div class="step"><div class="step-num">4</div><div><h3>Chat with AI</h3><p>Try: <code>I'm heading to the airport and have INR 800</code> or <code>I forgot my charger</code>. <strong>Expected:</strong> the assistant reasons about travel context, asks follow-up questions when needed, and does not invent products.</p></div></div>
      <div class="step"><div class="step-num">5</div><div><h3>Add to Cart</h3><p>Add a recommended item from the chat or a product from Catalog. <strong>Expected:</strong> cart quantity, price, subtotal, and total update for the signed-in user.</p></div></div>
      <div class="callout"><strong>Optional AI translation demo:</strong> In AI Chat, choose Translate Conversation and enter a passenger message plus a driver reply. GPT-5.6 is used when <code>OPENAI_API_KEY</code> is configured; otherwise the UI identifies the fallback behavior.</div>`, 1, total),
    page(`<div class="eyebrow">Completion</div><h2>Finish the Commerce Journey</h2><div class="accent-line"></div>
      <div class="step"><div class="step-num">6</div><div><h3>Checkout</h3><p>Open Cart and complete checkout. <strong>Expected:</strong> the Cart service sends the authenticated checkout request, an order is created, and the cart is emptied after success.</p></div></div>
      <div class="step"><div class="step-num">7</div><div><h3>Mock Payment</h3><p>Select UPI, credit card, debit card, or wallet and finish the payment. <strong>Expected:</strong> payment status becomes <code>PAID</code>, the UI shows success, and the order becomes <code>CONFIRMED</code>.</p></div></div>
      <div class="step"><div class="step-num">8</div><div><h3>Order Tracking</h3><p>Open Orders or the order detail. <strong>Expected:</strong> accurate items, quantities, totals, payment information, and tracking progression: <code>PLACED -> CONFIRMED -> HANDED_OVER -> COMPLETED</code>.</p></div></div>
      <h2 style="margin-top:15px">Useful API Checks</h2>
      <table><tr><th>Purpose</th><th>Endpoint</th><th>Expected</th></tr><tr><td>Catalog browse</td><td><code>GET :8083/api/v1/catalog/products</code></td><td>200</td></tr><tr><td>AI chat</td><td><code>POST :8086/api/v1/ai/chat</code></td><td>200 with valid JWT</td></tr><tr><td>Cart checkout</td><td><code>POST :8084/api/v1/carts/me/checkout</code></td><td>200 / 201 with valid JWT</td></tr><tr><td>Payment</td><td><code>POST :8087/api/v1/payments</code></td><td>200 / 201</td></tr><tr><td>Order</td><td><code>GET :8085/api/v1/orders/{id}</code></td><td>200 for owner</td></tr></table>
      <div class="callout warn"><strong>Judge preparation:</strong> replace package placeholders with current screenshots, a public repository URL, a demo video URL, and any temporary demo credentials you intentionally choose to provide.</div>
      <h2 style="margin-top:13px">Success Criteria</h2><div class="grid-3"><div class="card"><h3>Secure</h3><p>Protected actions require the same JWT accepted by the backend services.</p></div><div class="card"><h3>Grounded</h3><p>AI recommendations reference only active catalog products.</p></div><div class="card"><h3>Complete</h3><p>One continuous flow reaches a paid, confirmed, trackable order.</p></div></div>`, 2, total)
  ];
  write("Html/Demo-Guide.html", documentShell("InRideMart Demo Guide", pages));
}

function stackCard(mark, title, body) {
  return `<div class="card stack-card"><div class="mark">${mark}</div><div><h3>${title}</h3><p>${body}</p></div></div>`;
}

function generateTechStackPdf() {
  const total = 2;
  const pages = [
    page(`<div class="eyebrow">Engineering Stack</div><h1 style="font-size:27pt">Technology Stack</h1><p class="lede">A pragmatic Java microservice backend, a responsive Next.js frontend, and isolated AI orchestration.</p><div class="accent-line"></div>
      <h2>Backend and Data</h2><div class="grid-3">
      ${stackCard("J", "Java 21", "Primary language for the backend services and shared engineering conventions.")}
      ${stackCard("SB", "Spring Boot 3.x", "Independent REST services with configuration profiles, validation, and application lifecycle support.")}
      ${stackCard("SS", "Spring Security", "JWT-protected API boundaries and stateless security configuration.")}
      ${stackCard("PG", "PostgreSQL", "Relational persistence, managed through JPA/Hibernate and Flyway migrations.")}
      ${stackCard("FL", "Flyway", "Versioned database migration support for service-owned schemas.")}
      ${stackCard("RD", "Redis", "Available through Docker Compose as local platform infrastructure.")}
      ${stackCard("KF", "Kafka", "Available through Docker Compose as local platform infrastructure.")}
      ${stackCard("DK", "Docker Compose", "Starts PostgreSQL, Redis, and Kafka consistently for local development.")}
      ${stackCard("MV", "Maven", "Monorepo build, dependency management, and service-specific execution.")}
      </div>`, 1, total),
    page(`<div class="eyebrow">Experience, Integration, and AI</div><h2>Frontend and Platform Tooling</h2><div class="accent-line"></div><div class="grid-3">
      ${stackCard("NX", "Next.js", "App Router frontend with a same-origin API proxy and protected-route experience.")}
      ${stackCard("R", "React", "Component-based interface for catalog, cart, AI chat, payment, and tracking flows.")}
      ${stackCard("TS", "TypeScript", "Safer frontend contracts and component implementation.")}
      ${stackCard("TW", "Tailwind CSS", "Responsive, utility-led presentation system for the frontend.")}
      ${stackCard("OA", "OpenAI API", "The Responses API is accessed only inside the dedicated AI service.")}
      ${stackCard("G5", "GPT-5.6", "Conversation reasoning, intent detection, follow-up questions, and multilingual translation when configured.")}
      ${stackCard("CX", "Codex", "Accelerated implementation, API integration, security diagnosis, tests, UI iteration, and documentation.")}
      ${stackCard("REST", "REST APIs", "Clear service contracts used by the web client and reusable by future native clients.")}
      ${stackCard("JWT", "JWT", "Auth-issued Bearer tokens authenticated across protected backend services.")}
      ${stackCard("API", "Swagger / OpenAPI", "Interactive API documentation where Springdoc is configured: Auth, Customer, Catalog, Order, and AI.")}
      </div>
      <div class="callout" style="margin-top:14px"><strong>Why this shape:</strong> product facts remain in Catalog, cart operations remain in Cart, checkout and tracking remain in Order, mock payment remains in Payment, and OpenAI interaction remains in AI. The frontend composes those existing APIs instead of duplicating backend logic.</div>
      <h2 style="margin-top:14px">Security and Testing Baseline</h2><div class="grid-2"><div class="card"><h3>JWT + Spring Security</h3><p>Security is stateless. The frontend proxy forwards the Authorization header, and protected services validate the token issued by Auth.</p></div><div class="card"><h3>Quality Tooling</h3><p>The project uses JUnit 5, Mockito, REST Assured where applicable, frontend lint/build checks, and integration-flow verification across services.</p></div></div>`, 2, total)
  ];
  write("Html/Tech-Stack.html", documentShell("InRideMart Technology Stack", pages));
}

function generateInnovationPdf() {
  const total = 2;
  const pages = [
    page(`<div class="eyebrow">Why InRideMart</div><h1 style="font-size:27pt">Innovation Overview</h1><p class="lede">InRideMart makes a short ride a useful commerce moment without treating the passenger as a generic shopping session.</p><div class="accent-line"></div>
      <div class="grid-2"><div class="card highlight"><div class="quote">&ldquo;</div><h3>The real-world gap</h3><p>Many needs appear after pickup: a phone is running out of charge, rain begins, a passenger is hungry, a long airport ride needs comfort, or communication with the driver is difficult. The value lies in timing and relevance, not a huge catalog.</p></div><div class="card coral"><div class="quote">&ldquo;</div><h3>Why quick commerce is not enough</h3><p>Standard quick-commerce experiences optimize warehouse delivery and broad browsing. They do not naturally incorporate journey duration, destination, weather, rider intent, the immediate cab inventory, or passenger-driver communication within a short ride window.</p></div></div>
      <h2 style="margin-top:14px">How InRideMart Is Different</h2><div class="grid-3"><div class="card"><div class="icon">RC</div><h3>Ride Context</h3><p>Recommendations can use destination, duration, weather, travel purpose, budget, and time-of-day context.</p></div><div class="card"><div class="icon coral">AV</div><h3>Availability-Aware</h3><p>Catalog results can be filtered to an assigned demo cab inventory so suggestions stay practical for the ride.</p></div><div class="card"><div class="icon gold">MX</div><h3>Multilingual</h3><p>The same assistant can support passenger-driver translation alongside shopping assistance.</p></div></div>
      <div class="callout"><strong>Core principle:</strong> the AI is a concierge layer, not an independent product source. It reasons over intent and context, then recommends only products known to the Catalog service.</div>`, 1, total),
    page(`<div class="eyebrow">AI, Revenue, and Scale</div><h2>Why AI Is Central</h2><div class="accent-line"></div>
      <div class="grid-2"><div class="card"><h3>Conversational Intelligence</h3><p>GPT-5.6 lets passengers express a need in ordinary language. The assistant can greet, clarify ambiguity, understand multiple context signals, explain why an item fits, suggest a bundle, and avoid a one-size-fits-all product list.</p></div><div class="card"><h3>Trust Through Grounding</h3><p>Catalog search and deterministic matching keep recommendations tied to product names, categories, descriptions, prices, and availability. If an item is unavailable, the assistant should say so and suggest the closest catalog-grounded alternative.</p></div></div>
      <h2 style="margin-top:13px">Revenue Opportunities</h2><div class="grid-3"><div class="card"><h3>Convenience Margin</h3><p>Direct margin on high-intent, low-friction convenience items sold during the trip.</p></div><div class="card"><h3>Bundles</h3><p>Contextual airport, rain, charging, and road-trip packs can raise basket value while remaining useful.</p></div><div class="card"><h3>Partner Discovery</h3><p>Future inventory and brand partnerships can support sponsored, relevant placement with clear user controls.</p></div></div>
      <h2 style="margin-top:14px">Scale Path</h2><div class="roadmap"><strong>Phase 1</strong><div>Validate the passenger experience with demo cab inventory, service-owned checkout, mock payments, and catalog-grounded AI.</div></div><div class="roadmap"><strong>Phase 2</strong><div>Connect live cab inventory, driver operations, fulfillment events, and real payment processing.</div></div><div class="roadmap"><strong>Phase 3</strong><div>Expand to native mobile clients, consent-based personalization, voice, supply analytics, and multi-city operations.</div></div>
      <div class="callout warn"><strong>Responsible AI boundary:</strong> OpenAI is invoked through one dedicated service, configured by environment variable. It guides conversation and translation but does not fabricate inventory, prices, stock, or transaction state.</div>`, 2, total)
  ];
  write("Html/Innovation.html", documentShell("InRideMart Innovation", pages));
}

function generateQuickStartPdf() {
  const pages = [page(`<div class="eyebrow">OpenAI Build Week | Judge Handout</div><h1 style="font-size:25pt">InRideMart: Quick Start</h1><p class="lede">An AI-powered in-ride shopping concierge that brings context-aware discovery, checkout, mock payment, and tracking into one ride-time experience.</p><div class="accent-line"></div>
      <div class="grid-2"><div class="card highlight"><h3>Submission Links</h3><p><strong>GitHub:</strong><br/><code>REPLACE WITH PUBLIC REPOSITORY URL</code></p><p><strong>Demo video:</strong><br/><code>REPLACE WITH YOUTUBE / LOOM URL</code></p><p><strong>Frontend:</strong><br/><code>http://localhost:3000</code></p></div><div class="card coral"><h3>Test Account</h3><p><strong>Email:</strong> <code>REPLACE WITH DEMO EMAIL</code></p><p><strong>Password:</strong> <code>REPLACE WITH DEMO PASSWORD</code></p><p class="tiny">Or create a fresh account in the registration flow. Do not place personal or production credentials in this package.</p></div></div>
      <h2 style="margin-top:12px">Run Locally</h2><pre>git clone &lt;REPLACE-WITH-REPOSITORY-URL&gt;
cd InRideMart
$env:OPENAI_API_KEY = "&lt;optional-key-for-live-AI&gt;"
.\\start-inridemart.ps1</pre>
      <div class="grid-2"><div class="card"><h3>Default Ports</h3><p>Frontend 3000 | Auth 8081 | Customer 8082 | Catalog 8083 | Cart 8084 | Order 8085 | AI 8086 | Payment 8087</p><p class="tiny">Docker infrastructure: PostgreSQL 5433, Redis 6379, Kafka 9092.</p></div><div class="card"><h3>Swagger</h3><p><code>:8081/swagger-ui.html</code><br/><code>:8082/swagger-ui.html</code><br/><code>:8083/swagger-ui.html</code><br/><code>:8085/swagger-ui.html</code><br/><code>:8086/swagger-ui.html</code></p></div></div>
      <h2 style="margin-top:11px">2-Minute Demo Flow</h2><div class="flow"><span class="node">Register / Login</span><span class="arrow">-></span><span class="node">Catalog</span><span class="arrow">-></span><span class="node primary">AI Chat</span><span class="arrow">-></span><span class="node">Cart</span><span class="arrow">-></span><span class="node coral">Checkout + Payment</span><span class="arrow">-></span><span class="node">Tracking</span></div>
      <p class="tiny">Try: <code>I'm heading to the airport and have INR 800</code>, add a returned catalog item, select any mock payment method, and observe the confirmed order timeline. For live GPT responses and translation, set <code>OPENAI_API_KEY</code>; otherwise use the documented catalog-grounded fallback behavior.</p>`, 1, 1, "Judges Quick Start")];
  write("Html/Judges-Quick-Start.html", documentShell("InRideMart Judges Quick Start", pages));
}

function generateManifest() {
  write("MANIFEST.md", `# InRideMart Submission Package\n\nThis package is prepared for an OpenAI Build Week / Devpost submission. It contains rendered PDF documents and intentionally labelled screenshot placeholders.\n\n## Deliverables\n\n- README.pdf - executive product and technical summary\n- Architecture-Diagram.pdf - one-page architecture overview\n- Feature-Overview.pdf - feature descriptions with screenshot placeholders\n- Demo-Guide.pdf - judge-friendly end-to-end walkthrough\n- Tech-Stack.pdf - technology choices and responsibilities\n- Innovation.pdf - problem, differentiation, revenue, and scale\n- Judges-Quick-Start.pdf - one-page handout\n- Screenshots/ - nine non-fake placeholders requiring replacement before public submission\n\n## Required Manual Replacements\n\n1. Replace the PNGs in \`Screenshots/\` with captures from the running application.\n2. Replace repository and demo-video placeholders in the PDF source before re-rendering if public links are available.\n3. Add intentionally created temporary demo credentials only if you want judges to use a pre-created account.\n\n## Regeneration\n\nSource HTML and the asset generator are retained under \`Source/\` and \`Html/\`. Run:\n\n\`node .\\Source\\generate-submission.js\`\n\nThen render the HTML files in \`Html/\` to PDF using a Chromium-based browser.\n\nNo application source files are included or changed by this package.\n`);
  write("Source/README.md", `# Submission Source\n\n\`generate-submission.js\` creates the document HTML, screenshot placeholders, and manifest. The PDFs are rendered from the generated HTML with local Google Chrome headless printing. The source is retained so placeholders and submission links can be replaced cleanly before final upload.\n`);
}

async function main() {
  ensure(packageRoot);
  ensure(screenshotsDir);
  ensure(htmlDir);
  await Promise.all(screenshotSpecs.map(([fileName, title, caption]) => createPlaceholder(fileName, title, caption)));
  generateReadmePdf();
  generateArchitecturePdf();
  generateFeaturePdf();
  generateDemoGuidePdf();
  generateTechStackPdf();
  generateInnovationPdf();
  generateQuickStartPdf();
  generateManifest();
  console.log(`Generated submission sources in ${packageRoot}`);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
