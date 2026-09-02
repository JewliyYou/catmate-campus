const TOKEN_KEY = 'catmate-token'

export function getToken(){ return localStorage.getItem(TOKEN_KEY) }
export function setToken(token){ token ? localStorage.setItem(TOKEN_KEY,token) : localStorage.removeItem(TOKEN_KEY) }

export async function request(path, options={}){
  const headers = { 'Content-Type':'application/json', ...(options.headers||{}) }
  const token=getToken(); if(token) headers.Authorization=`Bearer ${token}`
  const response=await fetch(`/api${path}`,{...options,headers})
  if(response.status===401){setToken(null);window.dispatchEvent(new Event('auth-expired'))}
  if(!response.ok){let data={};try{data=await response.json()}catch{};throw new Error(data.message||`请求失败（${response.status}）`)}
  return response.status===204?null:response.json()
}

export const api={
  login:(username,password)=>request('/auth/login',{method:'POST',body:JSON.stringify({username,password})}),
  register:(username,displayName,password)=>request('/auth/register',{method:'POST',body:JSON.stringify({username,displayName,password})}),
  me:()=>request('/auth/me'), logout:()=>request('/auth/logout',{method:'POST'}),
  dashboard:()=>request('/dashboard'), cats:()=>request('/cats'), createCat:data=>request('/cats',{method:'POST',body:JSON.stringify(data)}),
  rescues:()=>request('/rescues'), createRescue:data=>request('/rescues',{method:'POST',body:JSON.stringify(data)}), updateRescue:(id,data)=>request(`/rescues/${id}`,{method:'PUT',body:JSON.stringify(data)}), deleteRescue:id=>request(`/rescues/${id}`,{method:'DELETE'}), acceptRescue:id=>request(`/rescues/${id}/accept`,{method:'PATCH'}),
  volunteers:()=>request('/volunteers'), createVolunteer:data=>request('/volunteers',{method:'POST',body:JSON.stringify(data)}), updateVolunteer:(id,data)=>request(`/volunteers/${id}`,{method:'PUT',body:JSON.stringify(data)}), deleteVolunteer:id=>request(`/volunteers/${id}`,{method:'DELETE'}),
  adminMetrics:()=>request('/admin/metrics')
}
