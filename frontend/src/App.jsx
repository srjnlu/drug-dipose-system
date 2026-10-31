import { useEffect, useMemo, useState } from "react";
import {
  Activity, ArrowRight, Bell, Check, CheckCircle2, ChevronDown,
  CircleHelp, Clock3, ClipboardList, Filter, Heart, LayoutDashboard,
  Leaf, MapPin, Menu, PackageCheck, Pill, Plus, Search, ShieldCheck, Truck,
  Users, X,
} from "lucide-react";

const STATUSES = ["Submitted", "Scheduled", "Collected", "Disposed", "Needs attention"];
const TOKEN_KEY = "goodcycle.accessToken";
const ROLE_KEY = "goodcycle.demoRole";
const todayLabel = new Intl.DateTimeFormat("en", {
  weekday: "long",
  month: "long",
  day: "numeric",
}).format(new Date()).toUpperCase();

function displayId(id) {
  return `GC-${id.slice(0, 8).toUpperCase()}`;
}

async function apiRequest(path, { token, ...options } = {}) {
  const response = await fetch(path, {
    ...options,
    headers: {
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });
  const body = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(body.message || body.detail || `Request failed (${response.status}).`);
  return body;
}

function fromApiRequest(item) {
  const titles = (value) => value.toLowerCase().replaceAll("_", " ").replace(/\b\w/g, (letter) => letter.toUpperCase());
  const status = item.status === "NEEDS_ATTENTION" ? "Needs attention" : titles(item.status);
  return {
    id: item.id,
    name: item.customerName,
    phone: item.customerPhone,
    type: item.medicineType,
    quantity: item.quantity,
    method: item.method === "DROP_OFF" ? "Drop-off" : "Pickup",
    location: item.location,
    date: item.createdAt.slice(0, 10),
    status,
    note: item.note || "",
  };
}

function App() {
  const [requests, setRequests] = useState([]);
  const [token, setToken] = useState(() => sessionStorage.getItem(TOKEN_KEY) || "");
  const [role, setRole] = useState(() => sessionStorage.getItem(ROLE_KEY) || "Customer");
  const [page, setPage] = useState("Overview");
  const [query, setQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("All statuses");
  const [showForm, setShowForm] = useState(false);
  const [showOtp, setShowOtp] = useState(false);
  const [verified, setVerified] = useState(false);
  const [phone, setPhone] = useState("+91 98765 43210");
  const [otp, setOtp] = useState("");
  const [demoCode, setDemoCode] = useState("");
  const [formError, setFormError] = useState("");
  const [mobileNav, setMobileNav] = useState(false);
  const [notice, setNotice] = useState("");
  const [apiError, setApiError] = useState("");

  const isCustomer = role === "Customer";
  const filtered = useMemo(() => requests.filter((request) => {
    const matchesText = `${request.id} ${request.name} ${request.location} ${request.type}`.toLowerCase().includes(query.toLowerCase());
    const matchesStatus = statusFilter === "All statuses" || request.status === statusFilter;
    return matchesText && matchesStatus;
  }), [requests, query, statusFilter]);

  useEffect(() => {
    if (!token) {
      setRequests([]);
      setApiError("");
      return;
    }
    let active = true;
    apiRequest("/api/disposals", { token })
      .then((items) => {
        if (active) {
          setRequests(items.map(fromApiRequest));
          setApiError("");
        }
      })
      .catch((error) => {
        if (active) setApiError(error.message);
      });
    return () => { active = false; };
  }, [token]);

  function saveToken(value, nextRole = role) {
    sessionStorage.setItem(TOKEN_KEY, value);
    sessionStorage.setItem(ROLE_KEY, nextRole);
    setToken(value);
  }

  async function changeRole(next) {
    setRole(next);
    setPage(next === "Customer" ? "Overview" : "Requests");
    setMobileNav(false);
    setQuery("");
    setStatusFilter("All statuses");
    setApiError("");
    setRequests([]);
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(ROLE_KEY);
    setToken("");
    if (next === "Customer") {
      return;
    }
    try {
      const demoAuth = await apiRequest("/api/auth/local-token", {
        method: "POST",
        body: JSON.stringify({ phone: "+919876543210", role: next.toUpperCase() }),
      });
      saveToken(demoAuth.accessToken, next);
    } catch (error) {
      setApiError(error.message);
    }
  }

  async function requestOtp(event) {
    event.preventDefault();
    const cleaned = phone.replace(/\s/g, "");
    if (!/^\+[1-9]\d{7,14}$/.test(cleaned)) {
      setFormError("Enter a valid phone number, including country code.");
      return;
    }
    const normalized = cleaned;
    setPhone(normalized);
    try {
      const result = await apiRequest("/api/auth/otp/request", {
        method: "POST",
        body: JSON.stringify({ phone: normalized }),
      });
      setDemoCode(result.demoCode || "");
      setFormError("");
      setShowOtp(true);
    } catch (error) {
      setFormError(error.message);
    }
  }

  async function verifyOtp(event) {
    event.preventDefault();
    try {
      const result = await apiRequest("/api/auth/otp/verify", {
        method: "POST",
        body: JSON.stringify({ phone, code: otp }),
      });
      saveToken(result.accessToken, "Customer");
      setVerified(true);
      setShowOtp(false);
      setFormError("");
      setOtp("");
      setShowForm(true);
    } catch (error) {
      setFormError(error.message);
    }
  }

  async function submitRequest(event) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    try {
      const result = await apiRequest("/api/disposals", {
        token,
        method: "POST",
        body: JSON.stringify({
          customerName: String(data.get("name")).trim(),
          medicineType: String(data.get("medicineType")),
          quantity: String(data.get("quantity")),
          method: String(data.get("method")).toUpperCase().replace("-", "_"),
          location: String(data.get("location")).trim(),
          note: String(data.get("note") || "").trim(),
        }),
      });
      const next = fromApiRequest(result);
      setRequests((current) => [next, ...current]);
      setShowForm(false);
      setVerified(false);
      setFormError("");
      setPage("My requests");
      setNotice(`Request ${next.id.slice(0, 8)} submitted successfully.`);
      window.setTimeout(() => setNotice(""), 4000);
    } catch (error) {
      setFormError(error.message);
    }
  }

  async function updateStatus(id, status) {
    try {
      const result = await apiRequest(`/api/disposals/${id}/status`, {
        token,
        method: "PATCH",
        body: JSON.stringify({ status: status.toUpperCase().replaceAll(" ", "_") }),
      });
      const updated = fromApiRequest(result);
      setRequests((current) => current.map((request) => request.id === id ? updated : request));
      setNotice(`Request ${id.slice(0, 8)} updated to ${status.toLowerCase()}.`);
      window.setTimeout(() => setNotice(""), 3000);
    } catch (error) {
      setApiError(error.message);
    }
  }

  const nav = isCustomer
    ? [{ label: "Overview", icon: LayoutDashboard }, { label: "My requests", icon: ClipboardList }, { label: "Disposal guide", icon: CircleHelp }]
    : [{ label: "Overview", icon: LayoutDashboard }, { label: "Requests", icon: ClipboardList }, ...(role === "Admin" ? [{ label: "People", icon: Users }] : []), { label: "Disposal guide", icon: CircleHelp }];
  const heading = page === "Overview" ? (isCustomer ? "Good for you. Better for everyone." : `${role} overview`) : page;

  return (
    <div className="app-shell">
      <aside className={`sidebar ${mobileNav ? "sidebar-open" : ""}`}>
        <a className="brand" href="#" onClick={(event) => { event.preventDefault(); setPage("Overview"); }}>
          <span className="brand-mark"><Leaf size={21} strokeWidth={2.5} /></span>
          <span>goodcycle<span className="brand-dot">.</span><small>MEDICINE RETURN</small></span>
        </a>
        <div className="workspace-label">WORKSPACE</div>
        <nav className="side-nav" aria-label="Main navigation">
          {nav.map(({ label, icon: Icon }) => (
            <button key={label} className={`nav-item ${page === label ? "active" : ""}`} onClick={() => { setPage(label); setMobileNav(false); }}>
              <Icon size={18} /><span>{label}</span>{label === "Requests" && !isCustomer && <span className="nav-count">{requests.filter((r) => r.status === "Submitted").length}</span>}
            </button>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <div className="help-card"><span className="help-icon"><Heart size={16} /></span><strong>Small action.<br />Lasting impact.</strong><p>Every safe return helps keep medicine out of our water and soil.</p><span className="impact-label"><Leaf size={13} /> A healthier tomorrow</span></div>
          <div className="user-profile">
            <div className="avatar">{role === "Customer" ? "PS" : role === "Admin" ? "AD" : "CS"}</div>
            <div className="profile-copy"><strong>{role === "Customer" ? "Priya Sharma" : role === "Admin" ? "Aarav (Admin)" : "Support team"}</strong><span>{role} workspace</span></div>
            <ChevronDown size={16} className="profile-chevron" />
          </div>
        </div>
      </aside>

      {mobileNav && <button className="nav-scrim" aria-label="Close navigation" onClick={() => setMobileNav(false)} />}
      <main className="main-area">
        <header className="topbar">
          <button className="icon-button mobile-menu" aria-label="Open navigation" onClick={() => setMobileNav(true)}><Menu size={20} /></button>
          <div className="breadcrumb">Workspace <span>/</span> <b>{page}</b></div>
          <div className="top-actions">
            <span className="demo-pill"><span /> DEMO MODE</span>
            <button className="icon-button notification-button" aria-label="Notifications"><Bell size={18} /><i /></button>
            <label className="role-picker"><span>View as</span><select value={role} onChange={(event) => changeRole(event.target.value)} aria-label="Select demo role"><option>Customer</option><option>Support</option><option>Admin</option></select><ChevronDown size={14} /></label>
          </div>
        </header>

        <div className="content">
          {notice && <div className="toast" role="status"><CheckCircle2 size={17} />{notice}<button onClick={() => setNotice("")} aria-label="Dismiss"><X size={15} /></button></div>}
          {apiError && <div className="api-error" role="alert"><CircleHelp size={17} /><span>{apiError}</span><button onClick={() => setApiError("")}>Dismiss</button></div>}
          <div className="page-heading">
            <div><div className="eyebrow">{isCustomer ? todayLabel : `${role.toUpperCase()} WORKSPACE`}</div><h1>{heading}</h1><p>{page === "Overview" ? (isCustomer ? "Ready to give unused medicine a safer next step?" : "Here’s what’s happening across your collection program.") : page === "Disposal guide" ? "A few simple steps can help keep everyone safe." : page === "People" ? "Manage the people supporting safe returns." : "Track each medicine return from collection to verified disposal."}</p></div>
            {isCustomer && page !== "Disposal guide" && <button className="button button-primary" onClick={() => { setFormError(""); setShowForm(true); }}><Plus size={17} /> New return</button>}
          </div>

          {page === "Disposal guide" ? <Guide /> : page === "People" ? <People /> : (
            <>
              {page === "Overview" && <SummaryCards role={role} requests={requests} />}
              {page === "Overview" && isCustomer && <ImpactBanner requests={requests} />}
              <section className="panel request-panel">
                <div className="panel-heading">
                  <div><h2>{isCustomer ? "Your medicine returns" : "Return requests"}</h2><p>{isCustomer ? "Follow your requests through every step." : "Review and update collection requests."}</p></div>
                  {page === "Overview" && <button className="text-button" onClick={() => setPage(isCustomer ? "My requests" : "Requests")}>View all <ArrowRight size={15} /></button>}
                </div>
                <div className="table-toolbar">
                  <label className="search-field"><Search size={16} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder={isCustomer ? "Search your requests" : "Search requests, people, areas"} /></label>
                  <label className="filter-field"><Filter size={15} /><select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)} aria-label="Filter by status"><option>All statuses</option>{STATUSES.map((status) => <option key={status}>{status}</option>)}</select><ChevronDown size={14} /></label>
                </div>
                <RequestTable requests={page === "Overview" ? filtered.slice(0, isCustomer ? 3 : 5) : filtered} role={role} onStatus={updateStatus} />
              </section>
              <div className="safety-note"><ShieldCheck size={18} /><span><strong>Safety first.</strong> Never put medicines in household recycling or share them with another person. Follow your local pharmacy or authorized disposal partner’s instructions.</span></div>
            </>
          )}
          <footer className="footer"><span>© 2026 GoodCycle</span><span>Made for a healthier tomorrow <Leaf size={13} /></span><span><a href="#" onClick={(e) => e.preventDefault()}>Privacy</a><a href="#" onClick={(e) => e.preventDefault()}>Help center</a></span></footer>
        </div>
      </main>

      {showForm && <Modal title="Start a safe return" onClose={() => setShowForm(false)}>
        {!verified ? <form className="form-stack" onSubmit={requestOtp}>
          <div className="modal-intro"><span className="modal-icon"><ShieldCheck size={20} /></span><p>Verify your phone number to create a return request. <b>Demo mode:</b> the code is shown here, not sent by SMS.</p></div>
          <label>Phone number<input autoFocus type="tel" value={phone} onChange={(e) => setPhone(e.target.value)} placeholder="+91 98765 43210" /></label>
          {formError && <p className="form-error" role="alert">{formError}</p>}
          <button className="button button-primary button-full" type="submit">Show demo OTP <ArrowRight size={16} /></button>
        </form> : <form className="form-stack" onSubmit={submitRequest}>
          <div className="verified-banner"><CheckCircle2 size={17} /> Phone verified for this demo</div>
          <div className="form-grid">
            <label>Your name<input name="name" required placeholder="Full name" /></label>
            <label>Medicine type<select name="medicineType"><option>Tablets</option><option>Capsules</option><option>Syrup</option><option>Inhaler</option><option>Other</option></select></label>
            <label>Approximate amount<select name="quantity"><option>1–2 packs</option><option>3–5 packs</option><option>Small bag</option><option>One bottle</option></select></label>
            <label>Preferred method<select name="method"><option>Drop-off</option><option>Pickup</option></select></label>
            <label className="wide-field">Area or postal code<input name="location" required placeholder="e.g. Indiranagar, Bengaluru" /></label>
            <label className="wide-field">Anything the handler should know? <span className="optional">(optional)</span><textarea name="note" rows="2" placeholder="Keep details general. Do not enter medical or personal health information." /></label>
          </div>
          <div className="inline-safety"><ShieldCheck size={16} /> Keep medicines in original packaging. Do not include needles or sharps.</div>
          {formError && <p className="form-error" role="alert">{formError}</p>}
          <button className="button button-primary button-full" type="submit">Submit return request <ArrowRight size={16} /></button>
        </form>}
      </Modal>}

      {showOtp && <Modal title="Verify your phone" onClose={() => { setShowOtp(false); setOtp(""); }}>
        <form className="form-stack" onSubmit={verifyOtp}>
          {demoCode ? <div className="demo-code-box"><div><span className="demo-code-label">YOUR DEMO VERIFICATION CODE</span><strong>{demoCode}</strong></div><span className="demo-code-tag"><Check size={13} /> DEMO ONLY</span></div> : <div className="verified-banner"><CheckCircle2 size={17} /> Verification code sent to your phone</div>}
          <p className="otp-copy">{demoCode ? "Enter the 6-digit code above to continue. No SMS was sent." : "Enter the 6-digit code sent to your phone."}</p>
          <label>6-digit code<input autoFocus inputMode="numeric" maxLength="6" value={otp} onChange={(e) => setOtp(e.target.value.replace(/\D/g, ""))} placeholder="000000" /></label>
          {formError && <p className="form-error" role="alert">{formError}</p>}
          <button className="button button-primary button-full" type="submit">Verify & continue <ArrowRight size={16} /></button>
          <button className="text-button centered" type="button" onClick={() => { setShowOtp(false); setShowForm(true); }}>Back to phone number</button>
        </form>
      </Modal>}
    </div>
  );
}

function SummaryCards({ role, requests }) {
  const customer = role === "Customer";
  const completed = requests.filter((item) => item.status === "Disposed").length;
  const active = requests.filter((item) => !["Disposed", "Needs attention"].includes(item.status)).length;
  const cards = customer
    ? [{ label: "Your returns", value: requests.length, sub: "All-time requests", icon: PackageCheck, color: "mint" }, { label: "In progress", value: active, sub: "Being safely handled", icon: Activity, color: "blue" }, { label: "Safely disposed", value: completed, sub: "Verified returns", icon: Leaf, color: "peach" }]
    : [{ label: "Total requests", value: requests.length, sub: "All-time submissions", icon: ClipboardList, color: "mint" }, { label: "Need attention", value: requests.filter((r) => ["Submitted", "Needs attention"].includes(r.status)).length, sub: "Awaiting next action", icon: Clock3, color: "peach" }, { label: "Completed", value: completed, sub: "Verified disposal", icon: CheckCircle2, color: "blue" }];
  return <section className="summary-grid">{cards.map(({ label, value, sub, icon: Icon, color }) => <article className="summary-card" key={label}><div className={`summary-icon ${color}`}><Icon size={18} /></div><div className="summary-label">{label}</div><div className="summary-value">{value}</div><div className="summary-sub">{sub}</div><div className={`summary-accent ${color}`} /></article>)}</section>;
}

function ImpactBanner({ requests }) {
  return <section className="impact-banner"><div className="impact-art"><span className="impact-orbit orbit-one" /><span className="impact-orbit orbit-two" /><span className="impact-leaf"><Leaf size={29} /></span><span className="impact-spark">✳</span></div><div className="impact-copy"><div className="impact-kicker">YOUR IMPACT SO FAR</div><h3>One thoughtful return<br />makes a real difference.</h3><p>Proper disposal helps protect our communities and the environment.</p></div><div className="impact-stat"><strong>{requests.filter((r) => r.status === "Disposed").length}</strong><span>safe {requests.filter((r) => r.status === "Disposed").length === 1 ? "return" : "returns"} completed</span></div></section>;
}

function RequestTable({ requests, role, onStatus }) {
  const staff = role !== "Customer";
  if (!requests.length) return <div className="empty-state"><span><ClipboardList size={23} /></span><strong>No requests found</strong><p>Try a different search or status filter.</p></div>;
  return <div className="table-wrap"><table><thead><tr><th>REQUEST</th>{staff && <th>CUSTOMER</th>}<th>MEDICINE</th><th>METHOD / AREA</th><th>DATE</th><th>STATUS</th>{staff && <th>UPDATE</th>}</tr></thead><tbody>
    {requests.map((item) => <tr key={item.id}><td><span className="request-id">{displayId(item.id)}</span><span className="request-quantity">{item.quantity}</span></td>{staff && <td><span className="customer-name">{item.name}</span><span className="customer-phone">{item.phone}</span></td>}<td><span className="medicine-cell"><span className="medicine-icon"><Pill size={14} /></span>{item.type}</span><span className="request-quantity">{item.note}</span></td><td><span className="method-cell">{item.method === "Pickup" ? <Truck size={14} /> : <MapPin size={14} />}{item.method}</span><span className="request-quantity area-text">{item.location}</span></td><td><span className="date-cell">{new Date(`${item.date}T12:00:00`).toLocaleDateString("en", { month: "short", day: "numeric" })}</span><span className="request-quantity">{new Date(`${item.date}T12:00:00`).getFullYear()}</span></td><td><Status status={item.status} /></td>{staff && <td><label className="status-select"><span className="sr-only">Update status for {displayId(item.id)}</span><select value={item.status} onChange={(e) => onStatus(item.id, e.target.value)}>{STATUSES.map((status) => <option key={status}>{status}</option>)}</select><ChevronDown size={12} /></label></td>}</tr>)}
  </tbody></table></div>;
}

function Status({ status }) {
  const styles = { Submitted: "status-new", Scheduled: "status-scheduled", Collected: "status-collected", Disposed: "status-done", "Needs attention": "status-alert" };
  return <span className={`status-badge ${styles[status]}`}><i />{status}</span>;
}

function Guide() {
  const steps = [
    { n: "01", icon: PackageCheck, title: "Keep it in its packaging", text: "Leave tablets and other medicines in their original packaging when possible. This helps handlers identify what they are accepting." },
    { n: "02", icon: MapPin, title: "Choose an authorized return", text: "Use a participating pharmacy or approved collection partner. Pickup availability depends on your location." },
    { n: "03", icon: ShieldCheck, title: "Follow local instructions", text: "Never flush medicine or put it in household recycling. Ask a pharmacist or local waste authority about special items." },
  ];
  return <section className="guide-grid">{steps.map(({ n, icon: Icon, title, text }) => <article className="guide-card" key={n}><span className="guide-number">{n}</span><span className="guide-icon"><Icon size={21} /></span><h2>{title}</h2><p>{text}</p></article>)}<div className="guide-alert"><CircleHelp size={20} /><div><strong>Not sure what’s accepted?</strong><p>Contact a licensed pharmacy or your local waste authority before arranging a return. This demo does not confirm local service availability.</p></div></div></section>;
}

function People() {
  return <section className="panel people-panel"><div className="panel-heading"><div><h2>Workspace access</h2><p>Demo roles illustrate the product experience only.</p></div></div><div className="people-row"><div className="avatar">AD</div><div><strong>Admin workspace</strong><span>Manage operational queues and service configuration</span></div><span className="role-tag">ADMIN</span></div><div className="people-row"><div className="avatar support-avatar">CS</div><div><strong>Support workspace</strong><span>Help customers and coordinate safe collections</span></div><span className="role-tag">SUPPORT</span></div><div className="access-warning"><ShieldCheck size={16} /> Role switching is not authentication. A production system must enforce authorization on the server.</div></section>;
}

function Modal({ title, children, onClose }) {
  return <div className="modal-backdrop" role="presentation" onMouseDown={(e) => { if (e.target === e.currentTarget) onClose(); }}><section className="modal" role="dialog" aria-modal="true" aria-labelledby="modal-title"><div className="modal-heading"><div><span className="modal-eyebrow">GOODCYCLE · DEMO</span><h2 id="modal-title">{title}</h2></div><button className="icon-button" onClick={onClose} aria-label="Close dialog"><X size={19} /></button></div>{children}</section></div>;
}

export default App;
