import {Outlet, useNavigate} from 'react-router-dom'
import {useContext} from "react";
import {AuthContext} from "./context/AuthContext.tsx";

function App() {
    const navigate = useNavigate()
    const {isAuthenticated, logout} = useContext(AuthContext);

  return (
      <div className={`w-full h-screen bg-slate-50 text-slate-800 antialiased`}>

          <nav className={`sticky top-0 z-50 bg-white border-b border-slate-100 shadow-sm shadow-slate-200/40 px-6 py-4`}>

              <div className={`flex items-center justify-between max-w-6xl mx-auto`}>

                  <span onClick={() => navigate(isAuthenticated ? '/dashboard' : '/')}
                        className={`font-bold text-2xl text-sky-400 tracking-tight hover:text-sky-500 hover:scale-105 transition cursor-pointer`}
                  >NodeNotes</span>

                  {!isAuthenticated ? (
                      <div className={`flex items-center gap-4`}>

                          <div onClick={() => navigate("/login")}
                               className={`flex justify-center items-center gap-2 text-sm font-medium hover:text-slate-900 hover:scale-105 cursor-pointer transition`}>

                              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"
                                   stroke="currentColor" className="w-5 h-5">
                                  <path stroke-linecap="round" stroke-linejoin="round"
                                        d="M15.75 9V5.25A2.25 2.25 0 0 0 13.5 3h-6a2.25 2.25 0 0 0-2.25 2.25v13.5A2.25 2.25 0 0 0 7.5 21h6a2.25 2.25 0 0 0 2.25-2.25V15M12 9l-3 3m0 0 3 3m-3-3h12.75"/>
                              </svg>


                              Login

                          </div>

                          <div onClick={() => navigate("/register")}
                               className={`flex justify-center items-center gap-2 text-sm font-medium text-white bg-sky-400 hover:bg-sky-500 hover:scale-105 px-4 py-2 rounded-lg transition cursor-pointer`}>

                              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"
                                   stroke="currentColor" className="w-5 h-5">
                                  <path stroke-linecap="round" stroke-linejoin="round"
                                        d="M18 7.5v3m0 0v3m0-3h3m-3 0h-3m-2.25-4.125a3.375 3.375 0 1 1-6.75 0 3.375 3.375 0 0 1 6.75 0ZM3 19.235v-.11a6.375 6.375 0 0 1 12.75 0v.109A12.318 12.318 0 0 1 9.374 21c-2.331 0-4.512-.645-6.374-1.766Z"/>
                              </svg>


                              Register

                          </div>

                      </div>
                  ) : (
                      <div className={`flex items-center gap-4`}>

                          <button onClick={() => navigate("/profile")}
                                  className={`flex items-center justify-center gap-2 text-sm font-medium hover:scale-105 hover:text-slate-900 cursor-pointer transition`}>

                              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"
                                   stroke="currentColor" className="w-5 h-5">
                                  <path stroke-linecap="round" stroke-linejoin="round"
                                        d="M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.501 20.118a7.5 7.5 0 0 1 14.998 0A17.933 17.933 0 0 1 12 21.75c-2.676 0-5.216-.584-7.499-1.632Z"/>
                              </svg>

                              My Profile

                          </button>


                          <button onClick={() => {
                              logout()
                              navigate("/login")
                          }}
                                  className={`flex items-center justify-center gap-2 text-sm font-medium text-white bg-rose-400 hover:bg-rose-500 hover:scale-105 px-4 py-2 rounded-lg transition cursor-pointer`}>

                          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"
                                   stroke="currentColor" className="w-5 h-5">
                                  <path stroke-linecap="round" stroke-linejoin="round"
                                        d="M8.25 9V5.25A2.25 2.25 0 0 1 10.5 3h6a2.25 2.25 0 0 1 2.25 2.25v13.5A2.25 2.25 0 0 1 16.5 21h-6a2.25 2.25 0 0 1-2.25-2.25V15m-3 0-3-3m0 0 3-3m-3 3H15"/>
                              </svg>

                              Log out

                          </button>


                      </div>
                  )}

              </div>

          </nav>

          <main className={`max-w-6xl mx-auto p-6`}>
              <Outlet/>
          </main>
      </div>
  )
}

export default App
