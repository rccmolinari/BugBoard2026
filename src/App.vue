<!--
  App.vue
  Placeholder per il routing.

  Non ho aggiunto vue-router come da tue istruzioni: lo cablerai tu.
  Qui di default viene montata LoginPage così puoi avviare subito `npm run dev`
  e vedere qualcosa. Quando aggiungi vue-router sostituisci il componente
  con `<router-view />` e dichiara le rotte nel tuo main.js (o in router/index.js).

  Le tre pagine sono già import-ready:
    import LoginPage     from './pages/LoginPage.vue'
    import DashboardPage from './pages/DashboardPage.vue'
    import AdminPage     from './pages/AdminPage.vue'

  Nota: i redirect dentro alle pagine usano ancora window.location.href
  (es. 'login.html', 'dashboard.html', 'admin.html') — era la logica originale
  che mi avevi chiesto di non toccare. Quando collegherai il router ti basterà
  sostituirli con router.push('/login'), router.push('/dashboard'), ecc.
-->
<script setup>
import LoginPage from './pages/LoginPage.vue'
// import DashboardPage from './pages/DashboardPage.vue'
// import AdminPage from './pages/AdminPage.vue'
</script>

<template>
  <router-view />
</template>
