#!/usr/bin/env python3
"""
Generates target/test-report/index.html from surefire XML + jacoco.csv.
Run automatically via exec-maven-plugin in the verify phase.
"""
import csv
import glob
import os
import sys
import xml.etree.ElementTree as ET
from datetime import date

TARGET = sys.argv[1] if len(sys.argv) > 1 else "target"
PROJECT_NAME = sys.argv[2] if len(sys.argv) > 2 else "quarkusdroneshop"
SUREFIRE_DIR = os.path.join(TARGET, "surefire-reports")

# Prefer merged report (counter uses site/jacoco-merged); fall back to standard path
_JACOCO_CANDIDATES = [
    os.path.join(TARGET, "site", "jacoco-merged", "jacoco.csv"),
    os.path.join(TARGET, "jacoco-report", "jacoco.csv"),
]
JACOCO_CSV = next((p for p in _JACOCO_CANDIDATES if os.path.exists(p) and os.path.getsize(p) > 0), _JACOCO_CANDIDATES[-1])
OUT_DIR      = os.path.join(TARGET, "test-report")
OUT_FILE     = os.path.join(OUT_DIR, "index.html")

os.makedirs(OUT_DIR, exist_ok=True)

# ── Parse surefire XML ───────────────────────────────────────────────────────
suites = []
for xml_path in sorted(glob.glob(os.path.join(SUREFIRE_DIR, "TEST-*.xml"))):
    root = ET.parse(xml_path).getroot()
    suite = {
        "name":  root.get("name", "").split(".")[-1],
        "pkg":   ".".join(root.get("name", "").split(".")[:-1]),
        "tests": int(root.get("tests", 0)),
        "pass":  0, "fail": 0, "skip": 0,
        "time":  float(root.get("time", 0)),
        "cases": []
    }
    for tc in root.findall("testcase"):
        if tc.find("failure") is not None or tc.find("error") is not None:
            status = "fail"
        elif tc.find("skipped") is not None:
            status = "skip"
        else:
            status = "pass"
        suite["cases"].append({
            "name":    tc.get("name", ""),
            "status":  status,
            "time":    float(tc.get("time", 0)),
            "message": (tc.find("failure") or tc.find("error") or ET.Element("x")).get("message", ""),
        })
        suite[status] += 1
    suites.append(suite)

total   = sum(s["tests"] for s in suites)
passed  = sum(s["pass"]  for s in suites)
failed  = sum(s["fail"]  for s in suites)
skipped = sum(s["skip"]  for s in suites)
elapsed = sum(s["time"]  for s in suites)
pct     = round(100 * passed / total) if total else 0

def fmt_time(t):
    return f"{t:.2f}s" if t >= 1 else f"{int(t*1000)}ms"

# ── Parse JaCoCo CSV ────────────────────────────────────────────────────────
cov_pkgs = {}
total_inst_m = total_inst_c = total_line_m = total_line_c = total_br_m = total_br_c = 0
if os.path.exists(JACOCO_CSV):
    with open(JACOCO_CSV) as f:
        for row in csv.DictReader(f):
            pkg = row["PACKAGE"].replace("/", ".")
            if pkg not in cov_pkgs:
                cov_pkgs[pkg] = {"classes": 0, "im": 0, "ic": 0, "lm": 0, "lc": 0, "bm": 0, "bc": 0}
            p = cov_pkgs[pkg]
            p["classes"] += 1
            p["im"] += int(row["INSTRUCTION_MISSED"]); p["ic"] += int(row["INSTRUCTION_COVERED"])
            p["lm"] += int(row["LINE_MISSED"]);        p["lc"] += int(row["LINE_COVERED"])
            p["bm"] += int(row["BRANCH_MISSED"]);      p["bc"] += int(row["BRANCH_COVERED"])
            total_inst_m += int(row["INSTRUCTION_MISSED"]); total_inst_c += int(row["INSTRUCTION_COVERED"])
            total_line_m += int(row["LINE_MISSED"]);        total_line_c += int(row["LINE_COVERED"])
            total_br_m   += int(row["BRANCH_MISSED"]);      total_br_c   += int(row["BRANCH_COVERED"])

def pct_str(c, t): return f"{100*c/t:.1f}" if t else "0.0"
inst_pct   = pct_str(total_inst_c, total_inst_m + total_inst_c)
line_pct   = pct_str(total_line_c, total_line_m + total_line_c)
branch_pct = pct_str(total_br_c,   total_br_m   + total_br_c)

# ── Build HTML ───────────────────────────────────────────────────────────────
def badge(status):
    icons = {
        "pass": '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg>',
        "fail": '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>',
        "skip": '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><line x1="5" y1="12" x2="19" y2="12"/></svg>',
    }
    labels = {"pass": "PASS", "fail": "FAIL", "skip": "SKIP"}
    return f'<span class="badge {status}">{icons[status]} {labels[status]}</span>'

# Suite summary rows
suite_rows = ""
for s in suites:
    p = round(100 * s["pass"] / s["tests"]) if s["tests"] else 0
    suite_rows += f"""<tr>
      <td><strong>{s['name']}</strong><br><span style="font-size:11px;color:var(--text-light)">{s['pkg']}</span></td>
      <td>{s['tests']}</td>
      <td style="color:var(--pass);font-weight:600">{s['pass']}</td>
      <td style="color:{'var(--fail)' if s['fail'] > 0 else 'var(--text-light)'};">{s['fail']}</td>
      <td>{fmt_time(s['time'])}</td>
      <td><div class="mini-bar"><div class="mini-fill" style="width:{p}%"></div></div></td>
    </tr>"""

# Suite detail (grouped by package)
pkgs: dict = {}
for s in suites:
    pkgs.setdefault(s["pkg"], []).append(s)

suites_html = ""
for pkg, ss in pkgs.items():
    pkg_total = sum(x["tests"] for x in ss)
    pkg_pass  = sum(x["pass"]  for x in ss)
    suites_html += f"""<div class="pkg-group">
      <div class="pkg-header" onclick="togglePkg(this)">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z"/></svg>
        {pkg}
        <span class="pkg-badge badge pass" style="margin-left:auto;">{pkg_pass}/{pkg_total}</span>
      </div>
      <div class="pkg-body">"""
    for s in ss:
        suites_html += f"""<div style="border-bottom:1px solid var(--border);">
          <div style="padding:8px 28px;background:#fcfcfd;font-size:12px;font-weight:600;display:flex;align-items:center;gap:8px;cursor:pointer;" onclick="toggleCases(this)">
            {badge('fail' if s['fail'] > 0 else 'pass')} {s['name']}
            <span style="color:var(--text-light);margin-left:auto;font-size:11px">{s['tests']} tests &nbsp; {fmt_time(s['time'])}</span>
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
          </div>
          <div class="cases-body" style="display:none;">"""
        for c in s["cases"]:
            msg = f'<div style="font-size:11px;color:var(--fail);padding:4px 0 0 26px;">{c["message"]}</div>' if c["message"] else ""
            suites_html += f"""<div class="test-row" style="padding-left:44px;flex-direction:column;align-items:flex-start;">
              <div style="display:flex;align-items:center;gap:10px;width:100%">
                {badge(c['status'])}
                <span class="test-name">{c['name']}</span>
                <span class="test-time" style="margin-left:auto">{fmt_time(c['time'])}</span>
              </div>{msg}
            </div>"""
        suites_html += "</div></div>"
    suites_html += "</div></div>"

# All tests
all_tests_html = ""
for s in suites:
    for c in s["cases"]:
        all_tests_html += f"""<div class="test-row" data-status="{c['status']}">
          {badge(c['status'])}
          <div style="flex:1">
            <div class="test-name">{c['name']}</div>
            <div class="test-class">{s['pkg']}.{s['name']}</div>
          </div>
          <span class="test-time">{fmt_time(c['time'])}</span>
        </div>"""

# Coverage rows
cov_rows = ""
color_for = lambda p: "var(--pass)" if float(p) >= 70 else ("#e67e22" if float(p) >= 40 else "var(--fail)")
for pkg, r in sorted(cov_pkgs.items()):
    ip = pct_str(r["ic"], r["im"] + r["ic"])
    lp = pct_str(r["lc"], r["lm"] + r["lc"])
    bp = pct_str(r["bc"], r["bm"] + r["bc"])
    cov_rows += f"""<tr>
      <td style="font-size:12px;font-family:monospace">{pkg}</td>
      <td>{r['classes']}</td>
      <td>
        <div style="font-size:11px;color:var(--text-light);margin-bottom:4px">{r['ic']} / {r['im']+r['ic']}</div>
        <div class="mini-bar" style="width:100px"><div class="mini-fill" style="width:{ip}%;background:{color_for(ip)}"></div></div>
        <div style="font-size:11px;font-weight:600;color:{color_for(ip)}">{ip}%</div>
      </td>
      <td>
        <div style="font-size:11px;color:var(--text-light);margin-bottom:4px">{r['lc']} / {r['lm']+r['lc']}</div>
        <div class="mini-bar" style="width:100px"><div class="mini-fill" style="width:{lp}%;background:{color_for(lp)}"></div></div>
        <div style="font-size:11px;font-weight:600;color:{color_for(lp)}">{lp}%</div>
      </td>
      <td>
        <div style="font-size:11px;color:var(--text-light);margin-bottom:4px">{r['bc']} / {r['bm']+r['bc']}</div>
        <div class="mini-bar" style="width:100px"><div class="mini-fill" style="width:{bp}%;background:{color_for(bp)}"></div></div>
        <div style="font-size:11px;font-weight:600;color:{color_for(bp)}">{bp}%</div>
      </td>
    </tr>"""

# Donut: circumference = 2*pi*50 ≈ 314.16
circ = 314.16
pass_arc = circ * pct / 100
fail_arc = circ - pass_arc
donut_color = "#27ae60" if pct == 100 else ("#f39c12" if pct >= 70 else "#e74c3c")

# ── Write HTML ───────────────────────────────────────────────────────────────
html = f"""<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>{PROJECT_NAME} — テスト結果レポート</title>
<style>
  :root {{
    --pass: #27ae60; --fail: #e74c3c; --skip: #f39c12;
    --bg: #f4f6f9; --card: #ffffff; --border: #dde3ec;
    --text: #2c3e50; --text-light: #7f8c8d; --accent: #3498db;
    --sidebar-bg: #1e2a3a;
  }}
  * {{ box-sizing: border-box; margin: 0; padding: 0; }}
  body {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: var(--bg); color: var(--text); display: flex; min-height: 100vh; }}
  .sidebar {{ width: 220px; background: var(--sidebar-bg); color: #ecf0f1; flex-shrink: 0; display: flex; flex-direction: column; }}
  .sidebar-logo {{ padding: 20px 16px 16px; border-bottom: 1px solid rgba(255,255,255,.1); }}
  .sidebar-logo .project {{ font-size: 13px; font-weight: 700; color: var(--accent); letter-spacing: .5px; text-transform: uppercase; }}
  .sidebar-logo .module {{ font-size: 17px; font-weight: 600; color: #fff; margin-top: 2px; }}
  .sidebar-logo .date {{ font-size: 11px; color: #95a5a6; margin-top: 4px; }}
  nav {{ padding: 12px 0; flex: 1; }}
  .nav-item {{ display: flex; align-items: center; gap: 10px; padding: 10px 16px; cursor: pointer; font-size: 13px; color: #bdc3c7; transition: background .15s; }}
  .nav-item:hover, .nav-item.active {{ background: rgba(255,255,255,.08); color: #fff; }}
  .nav-item.active {{ border-left: 3px solid var(--accent); }}
  .nav-item svg {{ width: 16px; height: 16px; flex-shrink: 0; }}
  .sidebar-footer {{ padding: 12px 16px; border-top: 1px solid rgba(255,255,255,.1); font-size: 11px; color: #7f8c8d; }}
  .main {{ flex: 1; overflow: auto; }}
  .page {{ display: none; }}
  .page.active {{ display: block; }}
  .page-header {{ background: var(--card); border-bottom: 1px solid var(--border); padding: 20px 28px; }}
  .page-header h1 {{ font-size: 20px; font-weight: 600; }}
  .page-header p {{ font-size: 13px; color: var(--text-light); margin-top: 4px; }}
  .content {{ padding: 24px 28px; }}
  .summary-strip {{ display: grid; grid-template-columns: repeat(5, 1fr); gap: 14px; margin-bottom: 24px; }}
  .strip-card {{ background: var(--card); border: 1px solid var(--border); border-radius: 10px; padding: 18px 16px; display: flex; flex-direction: column; align-items: center; gap: 4px; }}
  .strip-card .val {{ font-size: 30px; font-weight: 700; }}
  .strip-card .lbl {{ font-size: 12px; color: var(--text-light); font-weight: 500; }}
  .strip-card.pass .val {{ color: var(--pass); }}
  .strip-card.fail .val {{ color: var(--fail); }}
  .strip-card.skip .val {{ color: var(--skip); }}
  .strip-card.total .val {{ color: var(--accent); }}
  .strip-card.time .val {{ color: #8e44ad; }}
  .charts-row {{ display: grid; grid-template-columns: 300px 1fr; gap: 20px; margin-bottom: 24px; }}
  .chart-card {{ background: var(--card); border: 1px solid var(--border); border-radius: 10px; padding: 20px; }}
  .chart-card h3 {{ font-size: 14px; font-weight: 600; margin-bottom: 16px; }}
  .donut-wrap {{ display: flex; align-items: center; gap: 24px; }}
  .donut-legend {{ display: flex; flex-direction: column; gap: 10px; }}
  .legend-item {{ display: flex; align-items: center; gap: 8px; font-size: 13px; }}
  .legend-dot {{ width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }}
  .cov-bars {{ display: flex; flex-direction: column; gap: 14px; }}
  .cov-item label {{ font-size: 12px; font-weight: 600; color: var(--text-light); display: flex; justify-content: space-between; margin-bottom: 6px; }}
  .bar-track {{ background: #eef0f3; border-radius: 4px; height: 10px; overflow: hidden; }}
  .bar-fill {{ height: 100%; border-radius: 4px; }}
  .card {{ background: var(--card); border: 1px solid var(--border); border-radius: 10px; overflow: hidden; margin-bottom: 20px; }}
  .card-head {{ padding: 14px 18px; border-bottom: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; }}
  .card-head h3 {{ font-size: 14px; font-weight: 600; }}
  table {{ width: 100%; border-collapse: collapse; font-size: 13px; }}
  th {{ background: #f8f9fb; padding: 10px 14px; text-align: left; font-size: 11px; font-weight: 600; color: var(--text-light); text-transform: uppercase; letter-spacing: .5px; border-bottom: 1px solid var(--border); }}
  td {{ padding: 10px 14px; border-bottom: 1px solid #f0f2f5; vertical-align: middle; }}
  tr:last-child td {{ border-bottom: none; }}
  tr:hover td {{ background: #f8f9fb; }}
  .badge {{ display: inline-flex; align-items: center; gap: 5px; padding: 3px 9px; border-radius: 20px; font-size: 11px; font-weight: 700; letter-spacing: .3px; }}
  .badge svg {{ width: 10px; height: 10px; }}
  .badge.pass {{ background: #eafaf1; color: var(--pass); }}
  .badge.fail {{ background: #fdedec; color: var(--fail); }}
  .badge.skip {{ background: #fef9e7; color: var(--skip); }}
  .mini-bar {{ width: 80px; height: 6px; background: #eef0f3; border-radius: 3px; overflow: hidden; }}
  .mini-fill {{ height: 100%; background: var(--pass); border-radius: 3px; }}
  .test-row {{ display: flex; align-items: center; gap: 10px; padding: 9px 14px; border-bottom: 1px solid #f0f2f5; font-size: 13px; }}
  .test-row:last-child {{ border-bottom: none; }}
  .test-row:hover {{ background: #f8f9fb; }}
  .test-name {{ flex: 1; font-family: 'SF Mono','Consolas',monospace; font-size: 12px; }}
  .test-class {{ color: var(--text-light); font-size: 11px; }}
  .test-time {{ font-size: 11px; color: var(--text-light); min-width: 50px; text-align: right; }}
  .pkg-group {{ margin-bottom: 0; }}
  .pkg-header {{ padding: 9px 14px; background: #f8f9fb; font-size: 12px; font-weight: 600; color: var(--text-light); border-bottom: 1px solid var(--border); display: flex; align-items: center; gap: 8px; cursor: pointer; user-select: none; }}
  .pkg-header:hover {{ background: #eef0f3; }}
</style>
</head>
<body>
<aside class="sidebar">
  <div class="sidebar-logo">
    <div class="project">Quarkus Droneshop</div>
    <div class="module">{PROJECT_NAME}</div>
    <div class="date">{date.today()}</div>
  </div>
  <nav>
    <div class="nav-item active" onclick="showPage('overview',this)">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>概要
    </div>
    <div class="nav-item" onclick="showPage('suites',this)">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11"/></svg>テストスイート
    </div>
    <div class="nav-item" onclick="showPage('tests',this)">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg>全テスト一覧
    </div>
    <div class="nav-item" onclick="showPage('coverage',this)">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>カバレッジ
    </div>
  </nav>
  <div class="sidebar-footer">Maven Surefire + JaCoCo</div>
</aside>

<main class="main">
  <div id="page-overview" class="page active">
    <div class="page-header"><h1>テスト結果サマリ</h1><p>Maven Surefire / JaCoCo レポート &nbsp;·&nbsp; {date.today()}</p></div>
    <div class="content">
      <div class="summary-strip">
        <div class="strip-card total"><div class="val">{total}</div><div class="lbl">総テスト数</div></div>
        <div class="strip-card pass"><div class="val">{passed}</div><div class="lbl">成功</div></div>
        <div class="strip-card fail"><div class="val">{failed}</div><div class="lbl">失敗</div></div>
        <div class="strip-card skip"><div class="val">{skipped}</div><div class="lbl">スキップ</div></div>
        <div class="strip-card time"><div class="val">{fmt_time(elapsed)}</div><div class="lbl">実行時間</div></div>
      </div>
      <div class="charts-row">
        <div class="chart-card">
          <h3>テスト結果分布</h3>
          <div class="donut-wrap">
            <svg width="140" height="140" viewBox="0 0 140 140">
              <circle cx="70" cy="70" r="50" fill="none" stroke="#eef0f3" stroke-width="24"/>
              <circle cx="70" cy="70" r="50" fill="none" stroke="{donut_color}" stroke-width="24"
                stroke-dasharray="{pass_arc:.2f} {circ:.2f}" stroke-dashoffset="0" transform="rotate(-90 70 70)"/>
              {"" if failed == 0 else f'<circle cx="70" cy="70" r="50" fill="none" stroke="var(--fail)" stroke-width="24" stroke-dasharray="{fail_arc:.2f} {circ:.2f}" stroke-dashoffset="{-pass_arc:.2f}" transform="rotate(-90 70 70)"/>'}
              <text x="70" y="65" text-anchor="middle" font-size="22" font-weight="700" fill="{donut_color}">{pct}%</text>
              <text x="70" y="83" text-anchor="middle" font-size="11" fill="#7f8c8d">成功率</text>
            </svg>
            <div class="donut-legend">
              <div class="legend-item"><div class="legend-dot" style="background:#27ae60"></div>成功: {passed}</div>
              <div class="legend-item"><div class="legend-dot" style="background:#e74c3c"></div>失敗: {failed}</div>
              <div class="legend-item"><div class="legend-dot" style="background:#f39c12"></div>スキップ: {skipped}</div>
            </div>
          </div>
        </div>
        <div class="chart-card">
          <h3>コードカバレッジ (JaCoCo)</h3>
          <div class="cov-bars">
            <div class="cov-item">
              <label><span>命令カバレッジ</span><span>{total_inst_c} / {total_inst_m+total_inst_c}</span></label>
              <div class="bar-track"><div class="bar-fill" style="width:{inst_pct}%;background:var(--accent)"></div></div>
              <div style="font-size:12px;color:var(--accent);margin-top:4px;font-weight:600;">{inst_pct}%</div>
            </div>
            <div class="cov-item">
              <label><span>行カバレッジ</span><span>{total_line_c} / {total_line_m+total_line_c}</span></label>
              <div class="bar-track"><div class="bar-fill" style="width:{line_pct}%;background:var(--pass)"></div></div>
              <div style="font-size:12px;color:var(--pass);margin-top:4px;font-weight:600;">{line_pct}%</div>
            </div>
            <div class="cov-item">
              <label><span>分岐カバレッジ</span><span>{total_br_c} / {total_br_m+total_br_c}</span></label>
              <div class="bar-track"><div class="bar-fill" style="width:{branch_pct}%;background:#e67e22"></div></div>
              <div style="font-size:12px;color:#e67e22;margin-top:4px;font-weight:600;">{branch_pct}%</div>
            </div>
          </div>
        </div>
      </div>
      <div class="card">
        <div class="card-head"><h3>テストスイート別サマリ</h3></div>
        <table>
          <thead><tr><th>テストスイート</th><th>テスト数</th><th>成功</th><th>失敗</th><th>実行時間</th><th>進捗</th></tr></thead>
          <tbody>{suite_rows}</tbody>
        </table>
      </div>
    </div>
  </div>

  <div id="page-suites" class="page">
    <div class="page-header"><h1>テストスイート</h1><p>パッケージ別グループ</p></div>
    <div class="content"><div class="card">{suites_html}</div></div>
  </div>

  <div id="page-tests" class="page">
    <div class="page-header"><h1>全テスト一覧</h1><p>{total} 件のテスト</p></div>
    <div class="content">
      <div class="card">
        <div style="padding:10px 14px;border-bottom:1px solid var(--border);display:flex;gap:8px;">
          <input id="search-input" type="text" placeholder="テスト名で検索..."
            style="flex:1;padding:6px 10px;border:1px solid var(--border);border-radius:6px;font-size:13px;outline:none;">
          <select id="status-filter" style="padding:6px 10px;border:1px solid var(--border);border-radius:6px;font-size:13px;background:white;">
            <option value="all">すべて</option>
            <option value="pass">成功</option>
            <option value="fail">失敗</option>
            <option value="skip">スキップ</option>
          </select>
        </div>
        <div id="tests-list">{all_tests_html}</div>
      </div>
    </div>
  </div>

  <div id="page-coverage" class="page">
    <div class="page-header"><h1>コードカバレッジ詳細</h1><p>JaCoCo — パッケージ別</p></div>
    <div class="content">
      <div class="card">
        <table>
          <thead><tr><th>パッケージ</th><th>クラス</th><th>命令</th><th>行</th><th>分岐</th></tr></thead>
          <tbody>{cov_rows}</tbody>
        </table>
      </div>
    </div>
  </div>
</main>

<script>
function showPage(id, el) {{
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
  document.getElementById('page-' + id).classList.add('active');
  el.classList.add('active');
}}
function togglePkg(el) {{
  const b = el.nextElementSibling;
  b.style.display = b.style.display === 'none' ? '' : 'none';
}}
function toggleCases(el) {{
  const b = el.nextElementSibling;
  b.style.display = b.style.display === 'none' ? '' : 'none';
}}
const allRows = Array.from(document.querySelectorAll('#tests-list .test-row'));
document.getElementById('search-input').addEventListener('input', filter);
document.getElementById('status-filter').addEventListener('change', filter);
function filter() {{
  const q = document.getElementById('search-input').value.toLowerCase();
  const s = document.getElementById('status-filter').value;
  allRows.forEach(r => {{
    const match = (s === 'all' || r.dataset.status === s) && r.textContent.toLowerCase().includes(q);
    r.style.display = match ? '' : 'none';
  }});
}}
</script>
</body>
</html>"""

with open(OUT_FILE, "w", encoding="utf-8") as f:
    f.write(html)

print(f"[test-report] Generated: {OUT_FILE}  ({total} tests, {passed} passed)")
