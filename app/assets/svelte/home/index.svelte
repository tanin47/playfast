<script lang="ts">
import {getVersionedAsset, LOGGED_IN_USER} from "../common/globals";

let title = "Welcome to Our Application";
let description = "This is a Svelte-powered application built with Play Framework";
</script>

<div class="container mx-auto p-8 flex flex-col gap-2">
  <div class="flex items-center gap-3 mb-4">
    <img src={getVersionedAsset('images/logo.png')} class="h-[40px]"/>
    <h1 class="text-4xl font-bold">{title}</h1>
  </div>
  <p>{description}</p>
  {#if LOGGED_IN_USER}
    <p>You are logged in as {LOGGED_IN_USER.email}</p>
    <a class="link link-primary" href="/edit">Edit your profile</a>
    <a class="link link-primary" href="/logout">Log out</a>
  {:else}
    <a class="link link-primary" href="/login">Login</a>
  {/if}
</div>

<style lang="scss">
</style>
