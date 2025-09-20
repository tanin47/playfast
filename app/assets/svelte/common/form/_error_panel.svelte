<script lang="ts">
  export let errors: string[] = []
</script>

{#if errors.length > 0}
  <div class="text-error" data-test-id="error-panel">
    {#each errors as error, index (index)}
      <p>{error}</p>
    {/each}
  </div>
{/if}

<style lang="scss">
</style>
