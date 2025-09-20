<script lang="ts">
  export let isLoading = false
  export let disabled = false
  export let onClick: () => Promise<void>
  export let dataTestId: string | null = null
</script>

<button class="btn btn-neutral" data-test-id={dataTestId} disabled={isLoading || disabled}
        onclick={() => { void onClick() }}>
  {#if isLoading}
    <span class="loading loading-spinner"></span>
  {/if}
  <slot></slot>
</button>

<style lang="scss">
</style>
